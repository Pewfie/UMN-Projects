#include <errno.h>
#include <netdb.h>
#include <pthread.h>
#include <signal.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>

#include "connection_queue.h"
#include "http.h"

#define BUFSIZE 512
#define LISTEN_QUEUE_LEN 5
#define N_THREADS 5

int keep_going = 1;
const char *serve_dir;

void handle_sigint(int signo) {
    keep_going = 0;
}

void *thread_func(void *arg) {
    // arg passed in is always the connection queue
    connection_queue_t *queue = (connection_queue_t *) arg;
    // Get client off of queue
    int client_fd = connection_dequeue(queue);
    // If dequeue fails, shut down thread
    while (client_fd != -1) {
        // Get request from client
        char request[BUFSIZE];
        if (read_http_request(client_fd, request) == -1) {
            // Close client's connection but keep accepting new clients
            fprintf(stderr, "read_http_request failed\n");
            close(client_fd);
            client_fd = connection_dequeue(queue);
            continue;
        }

        // Create path from request
        char path[BUFSIZE];
        // Put serving directory in first part of path
        int i = 0;
        while ((i < (BUFSIZE - 1)) && (serve_dir[i] != '\0')) {
            path[i] = serve_dir[i];
            i++;
        }
        // Put requested file in second part of path
        int j = 0;
        while ((i < (BUFSIZE - 1)) && (request[j] != '\0')) {
            path[i] = request[j];
            i++;
            j++;
        }
        path[i] = '\0';

        // Write response to client
        if (write_http_response(client_fd, path) == -1) {
            // Close client's connection but keep accepting new clients
            fprintf(stderr, "write_http_response failed\n");
            close(client_fd);
            client_fd = connection_dequeue(queue);
            continue;
        }

        // Move onto next client
        close(client_fd);
        client_fd = connection_dequeue(queue);
    }

    // No return value to main thread
    return NULL;
}

int main(int argc, char **argv) {
    // First command is directory to serve, second command is port
    if (argc != 3) {
        printf("Usage: %s <directory> <port>\n", argv[0]);
        return 1;
    }
    serve_dir = argv[1];
    const char *port = argv[2];

    // Handle SIGINT to clean up before exiting
    struct sigaction sigact;
    sigact.sa_handler = handle_sigint;
    sigfillset(&sigact.sa_mask);
    sigact.sa_flags = 0;
    if (sigaction(SIGINT, &sigact, NULL) == -1) {
        perror("sigaction");
        return 1;
    }

    // Supply the hints for getaddrinfo
    struct addrinfo hints;
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_UNSPEC;        // IPv4 or IPv6
    hints.ai_socktype = SOCK_STREAM;    // TCP
    hints.ai_flags = AI_PASSIVE;        // Server side

    // Call getaddrinfo
    struct addrinfo *server;
    int addr_ret = getaddrinfo(NULL, port, &hints, &server);
    if (addr_ret != 0) {
        fprintf(stderr, "getaddrinfo failed: %s\n", gai_strerror(addr_ret));
        return 1;
    }

    // Get the server socket
    int sock_fd = socket(server->ai_family, server->ai_socktype, server->ai_protocol);
    if (sock_fd == -1) {
        perror("socket");
        freeaddrinfo(server);
        return 1;
    }

    // Bind socket to port
    if (bind(sock_fd, server->ai_addr, server->ai_addrlen) == -1) {
        perror("bind");
        freeaddrinfo(server);
        close(sock_fd);
        return 1;
    }
    freeaddrinfo(server);

    // Set socket to listen for accept
    if (listen(sock_fd, LISTEN_QUEUE_LEN) == -1) {
        perror("listen");
        close(sock_fd);
        return 1;
    }
    
    // Create and initialize connection queue
    connection_queue_t queue;
    if (connection_queue_init(&queue) == -1) {
        fprintf(stderr, "connection_queue_init failed");
        close(sock_fd);
        return 1;
    }
    
    // Block all signals for threads
    sigset_t old_set;
    sigset_t all_set;
    sigfillset(&all_set);
    sigprocmask(SIG_SETMASK, &all_set, &old_set);

    // Create thread pool
    pthread_t threads[N_THREADS];
    for (int i = 0; i < N_THREADS; i++) {
        int result = pthread_create(threads + i, NULL, thread_func, &queue);
        // Error checking for pthread_create
        if (result != 0) {
            fprintf(stderr, "pthread_create failed: %s\n", strerror(result));
            connection_queue_shutdown(&queue);
            for (int j = 0; j < i; j++) {
                pthread_join(threads[j], NULL);
            }
            connection_queue_free(&queue);
            close(sock_fd);
            return 1;
        }
    }

    // Restore old signal mask for main thread
    sigprocmask(SIG_SETMASK, &old_set, NULL);

    // Run server until SIGINT received
    while (keep_going != 0) {
        // Accept client
        int client_fd = accept(sock_fd, NULL, NULL);
        if (client_fd == -1) {
            // If error is not SIGINT, continue to accept clients
            if (errno != EINTR) {
                perror("accept");
                continue;
            } else {
                break;
            }
        }
        // Queue up client for thread to work on
        if (connection_enqueue(&queue, client_fd) == -1) {
            // If error occurs, need to shut down
            close(client_fd);
            break;
        }
    }

    // Shutdown connection queue
    if (connection_queue_shutdown(&queue) == -1) {
        fprintf(stderr, "connection_queue_shutdown failed");
        connection_queue_free(&queue);
        close(sock_fd);
        return 1;
    }

    // Join all worker threads as they finish work on connection queue
    for (int i = 0; i < N_THREADS; i++) {
        pthread_join(threads[i], NULL);
    }

    // Free connection queue
    if (connection_queue_free(&queue) == -1) {
        fprintf(stderr, "connection_queue_free failed");
        close(sock_fd);
        return 1;
    }

    // Close server socket
    if (close(sock_fd) == -1) {
        perror("close");
        return 1;
    }

    return 0;
}
