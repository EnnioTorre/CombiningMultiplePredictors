/*
 * CINI, Consorzio Interuniversitario Nazionale per l'Informatica
 * Copyright 2013 CINI and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

#pragma once
#ifndef _CONCURRENCY_CONTROL_H
#define _CONCURRENCY_CONTROL_H

#include "../application.h"

#define MY_VECTOR_CLOCK_INDEX(STATE)	((((state_type*)(STATE))->type.server_state).server_id) - (((state_type*)(STATE))->num_clients)
#define VECTOR_CLOCK_INDEX(STATE,SERVER_INDEX)	( (SERVER_INDEX) - (((state_type*)(STATE))->num_clients) )

typedef struct _CC_event_list *CC_event_list_pointer;
typedef struct _CC_event_list {
	event_content_type * event;
	CC_event_list_pointer next;
} CC_event_list;

typedef struct _CC_shared_lock_type *CC_shared_lock_type_pointer;
typedef struct _CC_shared_lock_type {
	int tx_id;
	CC_shared_lock_type_pointer next;
} CC_shared_lock_type;

typedef struct _CC_commit_log_entry *CC_commit_log_entry_pointer;
typedef struct _CC_commit_log_entry {
	int *commit_vector_clock;
	int concurrency_number;
	CC_commit_log_entry_pointer previous;
} CC_commit_log_entry;

typedef struct _CC_commit_queue_entry *CC_commit_queue_entry_pointer;
typedef struct _CC_commit_queue_entry{
	int tx_id;
	int ready_to_commit;
	int concurrency_number;
	int* vector_clock;
	data_set_entry* write_set;
	data_set_entry* read_set;
	CC_commit_queue_entry_pointer next;
}CC_commit_queue_entry;

typedef struct _CC_last_version_entry *CC_last_version_entry_pointer;
typedef struct _CC_last_version_entry{
	int version_number;
	int subversion_number;
	CC_last_version_entry_pointer next; //used only to put this struct in a linked list
}CC_last_version_entry;

typedef struct _cc_metadata {
	transaction_metadata * active_transaction[ACTIVE_TRANSACTION_TABLE_SIZE];
	CC_event_list *lock_waiting_event_list;
	CC_event_list *event_queue_L1;
	int *locks;
	CC_shared_lock_type **shared_locks;
	int lock_retry_num;
	CC_commit_log_entry *commit_log_list;
	CC_event_list* read_waiting_event_list;
	int* prepare_vector_clock;
	CC_commit_queue_entry* commit_queue;
	CC_last_version_entry* last_versions;
} cc_metadata;

#endif /* _CONCURRENCY_CONTROL_H */
