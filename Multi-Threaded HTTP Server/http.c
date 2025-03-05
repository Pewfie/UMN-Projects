#include <assert.h>
#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <sys/stat.h>
#include <string.h>
#include <unistd.h>
#include "http.h"

#define BUFSIZE 512

const char *get_mime_type(const char *file_extension) {
    if (strcmp(".txt", file_extension) == 0) {
        return "text/plain";
    } else if (strcmp(".html", file_extension) == 0) {
        return "text/html";
    } else if (strcmp(".jpg", file_extension) == 0) {
        return "image/jpeg";
    } else if (strcmp(".png", file_extension) == 0) {
        return "image/png";
    } else if (strcmp(".pdf", file_extension) == 0) {
        return "application/pdf";
    }

    return NULL;
}

int read_http_request(int fd, char *resource_name) {
    // Use stdio FILE * for 'fgets()'
    int sock_fd_copy = dup(fd);
    if (sock_fd_copy == -1) {
        perror("dup");
        return -1;
    }

    FILE *sock_stream = fdopen(sock_fd_copy, "r");
    if (sock_stream == NULL) {
        perror("fdopen");
        close(sock_fd_copy);
        return -1;
    }
    // Disable usual stdio buffering
    if (setvbuf(sock_stream, NULL, _IONBF, 0) != 0) {
        perror("setvbuf");
        fclose(sock_stream);
        return -1;
    }
    
    // Read request line, obtain resource name
    char buf[BUFSIZE];
    if (fgets(buf, BUFSIZE, sock_stream) == NULL) {
        if (ferror(sock_stream)) {
            // fgets error
            perror("fgets");
        } else {
            // Otherwise, EOF reached on first read
            fprintf(stderr, "Empty request\n");
        }
        fclose(sock_stream);
        return -1;
    }

    // Using strtok_r for future thread safety
    char *save_ptr;
    // First call should get "GET", which doesn't need to be saved
    strtok_r(buf, " ", &save_ptr);
    // Second call gets resource name
    char *token = strtok_r(NULL, " ", &save_ptr);
    strcpy(resource_name, token);


    // Read rest of header, ignoring the contents
    while (fgets(buf, BUFSIZE, sock_stream) != NULL) {
        if (strcmp(buf, "\r\n") == 0) {
            break;
        }
    }
    if (ferror(sock_stream)) {
        perror("fgets");
        return -1;
    }

    // Close duplicated file descriptor
    if (fclose(sock_stream) != 0) {
        perror("fclose");
        return -1;
    }

    return 0;
}

int write_http_response(int fd, const char *resource_path) {
    // Buffer to hold responses as they are sent
    char response[BUFSIZE];

    // stat() to check both file existence and file size
    struct stat stat_buf;
    if (stat(resource_path, &stat_buf) == -1) {
        // If file doesn't exist
        if (errno == ENOENT) {
            sprintf(response, "HTTP/1.0 404 Not Found\r\n"
                            "Content-Length: 0\r\n"
                            "\r\n");
        }
        else {
            perror("stat");
            return -1;
        }
    }
    else {
        unsigned size = stat_buf.st_size;
        // Get file type from file extension
        char *file_extension = strchr(resource_path, '.');
        const char *type = get_mime_type(file_extension);
        if (type == NULL) {
            fprintf(stderr, "Invalid file extension\n");
            return -1;
        }
        // Fill in the header for response
        sprintf(response, "HTTP/1.0 200 ok\r\n"
                            "Content-Type: %s\r\n"
                            "Content-Length: %u\r\n"
                            "\r\n", type, size);

        // Open file, already confirmed to exist with stat()
        FILE *fp = fopen(resource_path, "r");
        if (fd == -1) {
            perror("fopen");
            return -1;
        }

        // Get header length, rest of buffer should be filled with file contents
        int header_len = strlen(response);
        char *body = response + header_len;
        fread(body, 1, BUFSIZE - header_len, fp);
        if (ferror(fp)) {
            perror("fread");
            fclose(fp);
            return -1;
        }

        // Write first response that has header
        if (write(fd, response, BUFSIZE) == -1) {
            perror("write");
            fclose(fp);
            return -1;
        }

        // Read from file and write its contents
        while (fread(response, 1, BUFSIZE, fp) > 0) {
            if (write(fd, response, BUFSIZE) == -1) {
                perror("write");
                fclose(fp);
                return -1;
            }
        }
        if (ferror(fp)) {
            perror("fread");
            fclose(fp);
            return -1;
        }

        // Close file
        if (fclose(fp) != 0) {
            perror("fclose");
            return -1;
        }
    }



    return 0;
}
