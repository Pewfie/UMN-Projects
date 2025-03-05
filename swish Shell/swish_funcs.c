#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#include "string_vector.h"
#include "swish_funcs.h"

#define MAX_ARGS 10

/*
 * Helper function to run a single command within a pipeline. You should make
 * make use of the provided 'run_command' function here.
 * tokens: String vector containing the tokens representing the command to be
 * executed, possible redirection, and the command's arguments.
 * pipes: An array of pipe file descriptors.
 * n_pipes: Length of the 'pipes' array
 * in_idx: Index of the file descriptor in the array from which the program
 *         should read its input, or -1 if input should not be read from a pipe.
 * out_idx: Index of the file descriptor in the array to which the program
 *          should write its output, or -1 if output should not be written to
 *          a pipe.
 * Returns 0 on success or -1 on error.
 */
int run_piped_command(strvec_t *tokens, int *pipes, int n_pipes, int in_idx, int out_idx) {

    // If in_idx is not -1, use it for stdin
    if (in_idx >= 0) {
        if (dup2(pipes[in_idx], STDIN_FILENO) == -1) {
            perror("dup2");
            return -1;
        }
    }

    // If out_idx is not -1, use it for stdout
    if (out_idx >= 0) {
        if (dup2(pipes[out_idx], STDOUT_FILENO) == -1) {
            perror("dup2");
            return -1;
        }
    }

    // Exec command
    run_command(tokens);

    // If process reaches this point, error has occurred
    return -1;
}

int run_pipelined_commands(strvec_t *tokens) {
    // Used to store return value in order to wait on all children
    int ret_val = 0;

    // Number of pipes between processes
    int num_pipes = strvec_num_occurrences(tokens, "|");

    // Allocate memory for all pipes
    int *pipe_fds = malloc(sizeof(int) * 2 * num_pipes);

    // Index of the current "|" being resolved in tokens
    int curr_pipe_idx;

    // If there are no "|", need to start with first element in tokens,
    // so -1 is set for later logic
    if (num_pipes == 0) {
        curr_pipe_idx = -1;
    }
    else {
        curr_pipe_idx = strvec_find_last(tokens, "|");
    }

    // Index of last "|" that was resolved (end of current command)
    // Initially is the end of tokens
    int last_pipe_idx = tokens->length;
    
    // Values to hold what pipe to write to/read from in this command
    int write_to_pipe = -1;
    int read_from_pipe = -1;

    // Stores number of children created to wait on them all at the end
    int children_created = 0;

    // Create number of processes equal to number of "|" plus one
    // Starts at end and works towards first process
    for (int i = 0; i <= num_pipes; i++) {

        // On final iteration, no pipe should be used as input, and no new pipe
        // should be created
        if (i == num_pipes) {
            read_from_pipe = -1;
        }

        // Create new pipe to read from
        else {
            if (pipe(pipe_fds + (2 * i)) < 0) {
                perror("pipe");

                // The last iteration's write end must be close if one was
                // made (i.e. if this is not the first iteration)
                if (i > 0) {
                    close(pipe_fds[(2 * (i - 1)) + 1]);
                }
                free(pipe_fds);
                ret_val = -1;

                // Exit process creation logic since since error occurred, 
                // but don't return since children can be waited on first
                break;
            }

            // Read end of newly created pipe is used by this next process
            read_from_pipe = 2 * i;
        }

        pid_t child_pid = fork();

        // Error checking for fork
        if (child_pid == -1) {
            perror("fork");

            // Skip on last iteration, no new pipe made
            if (i != num_pipes) {
                close(pipe_fds[2 * i]);
                close(pipe_fds[(2 * i) + 1]);
            }

            if (i > 0) {
                close(pipe_fds[(2 * (i - 1)) + 1]);
            }

            free(pipe_fds);
            ret_val = -1;
            break;
        }

        // Child process
        if (child_pid == 0) {

            // Skip on last iteration, no new pipe made
            if (i != num_pipes) {

                // Close write end of pipe
                if (close(pipe_fds[(2 * i) + 1]) < 0) {
                    perror("close");
                    close(pipe_fds[2 * i]);

                    if (i > 0) {
                        close(pipe_fds[(2 * (i - 1)) + 1]);
                    }
                    free(pipe_fds);

                    exit(1);
                }
            }

            // Need to slice the tokens used for next command
            strvec_t tokens_to_pipe;
            if (strvec_slice(tokens, &tokens_to_pipe, curr_pipe_idx + 1,
                             last_pipe_idx) < 0) {
                if (i != num_pipes) {
                    close(pipe_fds[2 * i]);
                }
                if (i > 0) {
                    close(pipe_fds[(2 * (i - 1)) + 1]);
                }
                free(pipe_fds);

                exit(1);
            }
  
            // Run the command
            if (run_piped_command(&tokens_to_pipe, pipe_fds, num_pipes, 
                                  read_from_pipe, write_to_pipe) < 0) {
                if (i != num_pipes) {
                    close(pipe_fds[2 * i]);
                }
                if (i > 0) {
                    close(pipe_fds[(2 * (i - 1)) + 1]);
                }
                free(pipe_fds);

                // Have to clear the strvec created for the slicing
                strvec_clear(&tokens_to_pipe);

                exit(1);
            }
        }

        // Parent process
        if (child_pid > 0) {
            children_created++;

            if (i > 0) {
                // Close write end of last iteration's pipe
                if (close(pipe_fds[(2 * (i - 1)) + 1]) < 0) {
                    perror("close");
                    free(pipe_fds);
                    ret_val = -1;
                    break;
                }
            }

            // Skip on last iteration, no new pipes made and no need to set up
            // the next iteration
            if (i != num_pipes) {

                // Close read end of pipe
                if (close(pipe_fds[2 * i]) < 0) {
                    perror("close");
                    free(pipe_fds);
                    ret_val = -1;
                    break;
                }

                // End of next command should be start of this command
                last_pipe_idx = curr_pipe_idx;

                // Shorten tokens to not include already used commands
                strvec_take(tokens, curr_pipe_idx);

                // Next command should write to this pipe's write end
                write_to_pipe = (2 * i) + 1;

                // If next iteration will be the last, command should start from
                // beginning of tokens
                if ((i + 1) == num_pipes) {
                    curr_pipe_idx = -1;
                }

                // If there are still pipes to resolve, find the new last one
                else {
                    curr_pipe_idx = strvec_find_last(tokens, "|");
                }
            }
            
        }
    }

    // Wait on all children
    for (int i = 0; i <= children_created; i++) {

        int status;
        if (wait(&status) < 0) {
            ret_val = -1;
        }

        // Check exit code of children
        if (WEXITSTATUS(status) != 0) {
            ret_val = -1;
        }

    }

    // Have to free malloc'd pipe fd array
    free(pipe_fds);

    return ret_val;
}
