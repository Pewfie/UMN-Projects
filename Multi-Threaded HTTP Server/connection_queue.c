#include <stdio.h>
#include <string.h>
#include "connection_queue.h"

int connection_queue_init(connection_queue_t *queue) {
    memset(queue->client_fds, 0, CAPACITY * sizeof(int));
    queue->length = 0;
    queue->read_idx = 0;
    queue->write_idx = 0;
    queue->shutdown = 0;
    int result = pthread_mutex_init(&queue->mutex, NULL);
    if (result != 0) {
        fprintf(stderr, "pthread_mutex_init failed: %s\n", strerror(result));
        return -1;
    }
    result = pthread_cond_init(&queue->empty, NULL);
    if (result != 0) {
        fprintf(stderr, "pthread_cond_init failed: %s\n", strerror(result));
        pthread_mutex_destroy(&queue->mutex);
        return -1;
    }
    result = pthread_cond_init(&queue->full, NULL);
    if (result != 0) {
        fprintf(stderr, "pthread_cond_init failed: %s\n", strerror(result));
        pthread_cond_destroy(&queue->empty);
        pthread_mutex_destroy(&queue->mutex);
        return -1;
    }
    return 0;
}

int connection_enqueue(connection_queue_t *queue, int connection_fd) {
    // Hold mutex for thread safe enqueue
    int result = pthread_mutex_lock(&queue->mutex);
    if (result != 0) {
        fprintf(stderr, "pthread_mutex_lock failed: %s\n", strerror(result));
        return -1;
    }
    // If queue is full, wait on the 'full' cond var
    while (queue->length == CAPACITY) {
        // If queue is shutting down, return error without waiting
        if (queue->shutdown == 1) {
            pthread_mutex_unlock(&queue->mutex);
            return -1;
        }
        result = pthread_cond_wait(&queue->full, &queue->mutex);
        if (result != 0) {
            fprintf(stderr, "pthread_cond_wait for full failed: %s\n", 
                            strerror(result));
            pthread_mutex_unlock(&queue->mutex);
            return -1;
        }
    }

    // Save index for writing to avoid constant memory dereferencing
    int index = queue->write_idx;
    // Enqueue fd in fd array
    queue->client_fds[index] = connection_fd;
    queue->length++;
    index++;
    // If index is past end of array, circle back around to start
    if (index == CAPACITY) {
        index = 0;
    }
    queue->write_idx = index;

    // Signal threads that are waiting for a non-empty queue
    result = pthread_cond_signal(&queue->empty);
    if (result != 0) {
        fprintf(stderr, "pthread_cond_signal for empty failed: %s\n", 
                            strerror(result));
        pthread_mutex_unlock(&queue->mutex);
        return -1;
    }
    // Unlock mutex
    result = pthread_mutex_unlock(&queue->mutex);
    if (result != 0) {
        fprintf(stderr, "pthread_mutex_unlock failed: %s\n", 
                            strerror(result));
        return -1;
    }

    return 0;
}

int connection_dequeue(connection_queue_t *queue) {
    // Hold mutex for thread safe dequeue
    int result = pthread_mutex_lock(&queue->mutex);
    if (result != 0) {
        fprintf(stderr, "pthread_mutex_lock failed: %s\n", strerror(result));
        return -1;
    }
    // If queue is empty, wait on 'empty' cond var
    while (queue->length == 0) {
        // If queue is shutting down, return error without waiting
        if (queue->shutdown == 1) {
            pthread_mutex_unlock(&queue->mutex);
            return -1;
        }
        result = pthread_cond_wait(&queue->empty, &queue->mutex);
        if (result != 0) {
            fprintf(stderr, "pthread_cond_wait for full failed: %s\n", 
                            strerror(result));
            pthread_mutex_unlock(&queue->mutex);
            return -1;
        }

    }

    // Save index for reading to avoid constant memory dereferencing
    int index = queue->read_idx;
    // Get fd out of array
    int fd = queue->client_fds[index];
    queue->length--;
    index++;
    // If index is past end of array, circle back around to start
    if (index == CAPACITY) {
        index = 0;
    }
    queue->read_idx = index;

    // Signal threads that are waiting on a non-full queue
    result = pthread_cond_signal(&queue->full);
    if (result != 0) {
        fprintf(stderr, "pthread_cond_signal for full failed: %s\n", 
                            strerror(result));
        pthread_mutex_unlock(&queue->mutex);
        return -1;
    }
    // Unlock mutex
    result = pthread_mutex_unlock(&queue->mutex);
    if (result != 0) {
        fprintf(stderr, "pthread_mutex_unlock failed: %s\n", 
                            strerror(result));
        return -1;
    }

    return fd;
}

int connection_queue_shutdown(connection_queue_t *queue) {
    // Keep track of errors while still performing full shutdown
    int ret_val = 0;
    queue->shutdown = 1;
    // Send signals to all threads waiting on 'empty'
    int result = pthread_cond_broadcast(&queue->empty);
    if (result != 0) {
        fprintf(stderr, "pthread_cond_broadcast for empty failed: %s\n", 
                            strerror(result));
        ret_val = -1;
    }
    // Send signals to all threads waiting on 'full'
    result = pthread_cond_broadcast(&queue->full);
    if (result != 0) {
        fprintf(stderr, "pthread_cond_broadcast for full failed: %s\n", 
                            strerror(result));
        ret_val = -1;
    }
    
    return ret_val;
}

int connection_queue_free(connection_queue_t *queue) {
    // Keep track of errors, while still performing full deconstruction
    int ret_val = 0;
    int result = pthread_cond_destroy(&queue->full);
    if (result != 0) {
        fprintf(stderr, "pthread_cond_destroy failed: %s\n", strerror(result));
        ret_val = -1;
    }
    result = pthread_cond_destroy(&queue->empty);
    if (result != 0) {
        fprintf(stderr, "pthread_cond_destroy failed: %s\n", strerror(result));
        ret_val = -1;
    }
    result = pthread_mutex_destroy(&queue->mutex);
    if (result != 0) {
        fprintf(stderr, "pthread_mutex_destroy failed: %s\n", strerror(result));
        ret_val = -1;
    }
    return ret_val;
}
