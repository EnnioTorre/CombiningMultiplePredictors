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

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "concurrency-control.h"
#include "concurrency-control-functions.h"
#include "hashing.h"
#include "../states.h"

extern void *object_states[1024];

static int active_transaction_table_hash_function(int tx_id) {
	return tx_id % (ACTIVE_TRANSACTION_TABLE_SIZE - 1);
}

transaction_metadata *get_transaction_metadata(int tx_id, SERVER_lp_state_type *pointer) {
	int hash_table_bucket = active_transaction_table_hash_function(tx_id);
	if (!pointer || !pointer->cc_metadata)
		return NULL ;
	transaction_metadata * tmd = (transaction_metadata *) pointer->cc_metadata->active_transaction[hash_table_bucket];
	while (tmd != NULL ) {
		if (tmd->tx_id == tx_id) {
			return tmd;
		}
		tmd = tmd->next;
	}
	return NULL ;
}

int add_transaction_metadata(int tx_id, int local, int tx_run_number, state_type *state) {

	// Get the server configuration from simulation state
	SERVER_lp_state_type *pointer = &state->type.server_state;
	if (!get_transaction_metadata(tx_id, pointer)) {
		int hash_table_bucket = active_transaction_table_hash_function(tx_id);
		transaction_metadata * new_transaction_metadata = (transaction_metadata *) malloc(sizeof(transaction_metadata));
		new_transaction_metadata->tx_id = tx_id;
		new_transaction_metadata->executed_operations = 0;
		new_transaction_metadata->current_tx_run_number = tx_run_number;
		new_transaction_metadata->is_blocked = 0;
		new_transaction_metadata->expected_prepare_response_counter = 0;
		new_transaction_metadata->next = NULL;
		new_transaction_metadata->write_set = NULL;
		new_transaction_metadata->read_set = (data_set*) malloc(sizeof(data_set));
		new_transaction_metadata->read_set->first = NULL;

		new_transaction_metadata->read_set->last = NULL;
		new_transaction_metadata->read_vector_clock = (int*) malloc(sizeof(int) * state->num_servers);
		new_transaction_metadata->has_read_mask = (short*) malloc(sizeof(short) * state->num_servers);
		new_transaction_metadata->commit_vector_clock = (int*) malloc(sizeof(int) * state->num_servers);
		new_transaction_metadata->nodes_on_write_mask = (short*) malloc(sizeof(short) * state->num_servers);

		if (pointer->cc_metadata->active_transaction[hash_table_bucket] == NULL ) {
			pointer->cc_metadata->active_transaction[hash_table_bucket] = new_transaction_metadata;
			return 1;
		} else {
			transaction_metadata * tmd = (transaction_metadata *) pointer->cc_metadata->active_transaction[hash_table_bucket];
			while (tmd->next != NULL ) {
				if (tmd->tx_id == tx_id) {
					return -1;
				}
				tmd = tmd->next;
			}
			tmd->next = new_transaction_metadata;
			return 1;
		}
	}
	return 1;
}

int remove_transaction_metadata(int tx_id, SERVER_lp_state_type * pointer) {
	int hash_table_bucket = active_transaction_table_hash_function(tx_id);
	transaction_metadata * prev = NULL;
	transaction_metadata * tmd = (transaction_metadata *) pointer->cc_metadata->active_transaction[hash_table_bucket];
	while (tmd != NULL ) {
		if (tmd->tx_id == tx_id) {
			if (prev == NULL ) {
				pointer->cc_metadata->active_transaction[hash_table_bucket] = tmd->next;
			} else {
				prev->next = tmd->next;
			}
			free(tmd);
			return 1;
		}
		prev = tmd;
		tmd = tmd->next;
	}
	printf("ERROR: (remove_transaction_from_table):  no active transaction with tx_id %d\n", tx_id);
	return -1;

}

static int add_data_to_write_set(int tx_id, int client_id, SERVER_lp_state_type * pointer, int object_key_id) {
	data_set_entry * entry = (data_set_entry *) malloc(sizeof(data_set_entry));
	entry->object_key_id = object_key_id;
	entry->next = NULL;
	transaction_metadata * transaction = get_transaction_metadata(tx_id, pointer);
	if (transaction == NULL ) {
		printf("ERROR: no transaction found with id %d (from client id %d)\n", tx_id, client_id);
		exit(-1);
	}
	if (transaction->write_set == NULL ) {
		transaction->write_set = entry;
		return 1;
	}
	data_set_entry * current_entry = transaction->write_set;
	while (current_entry->next != NULL ) {
		if (current_entry->object_key_id == object_key_id) {
			return 0;
		}
		current_entry = current_entry->next;
	}

	current_entry->next = entry;

	return 1;
}

static int add_data_to_read_set(transaction_metadata * transaction, int client_id, SERVER_lp_state_type * pointer, int object_key_id) {
	data_set_entry * entry = (data_set_entry *) malloc(sizeof(data_set_entry));
	entry->object_key_id = object_key_id;
	entry->next = NULL;

	if (transaction->read_set->first == NULL ) {
		transaction->read_set->first = entry;
		transaction->read_set->last = entry;
		return 1;
	}
	transaction->read_set->last->next = entry;
	transaction->read_set->last = entry;
	return 1;
}

int remove_event_of_tx(CC_event_list ** list, int tx_id) {
	CC_event_list *temp_entry, *current_entry;
	if (*list == NULL )
		return 0;
	if ((*list)->event->applicative_content.tx_id == tx_id) {
		temp_entry = (*list);
		*list = (*list)->next;
		free(temp_entry);
	} else {
		current_entry = *list;
		while (current_entry->next != NULL ) {
			if (current_entry->next->event->applicative_content.tx_id == tx_id) {
				temp_entry = current_entry->next;
				current_entry->next = current_entry->next->next;
				free(temp_entry);
			}
			current_entry = current_entry->next;
		}
	}
	return 1;
}

static event_content_type *get_event_for_object_key_id(CC_event_list ** list, int object_key_id) {

	event_content_type *event_content = NULL;
	if (*list == NULL ) {
		return NULL ;
	} else if ((*list)->event->applicative_content.object_key_id == object_key_id) {
		event_content = (*list)->event;
		*list = (*list)->next;
		return event_content;
	} else {
		CC_event_list *current = (*list)->next;
		CC_event_list *prev = *list;
		while (current != NULL ) {
			if (current->event->applicative_content.object_key_id == object_key_id) {
				event_content = current->event;
				prev = prev->next;
				free(current);
				return event_content;
			} else {
				current = current->next;
				prev = prev->next;
			}
		}
		return NULL ;
	}
}

static void reschedule_event(state_type *state, double now, int object_key_id) {

	SERVER_lp_state_type *pointer = &state->type.server_state;
	event_content_type *event_content = NULL;

	do {
		event_content = get_event_for_object_key_id(&pointer->cc_metadata->lock_waiting_event_list, object_key_id);
		if (event_content != NULL ) {
			transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
			if (transaction != NULL )
				transaction->is_blocked = 0;

			ScheduleNewEvent(pointer->server_id, now, DELIVER_MESSAGE, event_content, sizeof(event_content_type));
			if (pointer->configuration.cc_verbose)
				printf("cc%d - event of transaction %d, op_type %d, object %d rescheduled at time %f\n", event_content->applicative_content.server_id, event_content->applicative_content.tx_id,
						event_content->applicative_content.op_type, event_content->applicative_content.op_number, now);
			remove_event_of_tx(&pointer->cc_metadata->lock_waiting_event_list, event_content->applicative_content.tx_id);
		}
	} while (event_content != NULL );
}

static void remove_read_locks(int tx_id, data_set_entry *data_set, state_type *state, double now) {

	SERVER_lp_state_type *pointer = &state->type.server_state;

	data_set_entry *data_entry = data_set;
	int object_key_id;

	if (data_entry != NULL ) {
		do {
			object_key_id = data_entry->object_key_id;
			int need_to_unlock = 0;
			if (pointer->configuration.concurrency_control_type == GMU_PRIMARY_OWNER)
				need_to_unlock = is_primary(object_key_id, pointer->server_id, state->num_servers, state->num_clients);
			else
				need_to_unlock = is_owner(object_key_id, pointer->server_id, state->num_servers, state->num_clients, state->object_replication_degree);
			if (need_to_unlock && pointer->cc_metadata->shared_locks[object_key_id] != NULL ) {

				CC_shared_lock_type *temp_entry, *current_entry;
				if (pointer->cc_metadata->shared_locks[object_key_id]->tx_id == tx_id) {
					temp_entry = pointer->cc_metadata->shared_locks[object_key_id];
					pointer->cc_metadata->shared_locks[object_key_id] = pointer->cc_metadata->shared_locks[object_key_id]->next;
					//Wake up waiting event for key 'object_key_id';
					reschedule_event(state, now, object_key_id);
					free(temp_entry);
					if (pointer->configuration.cc_verbose)
						printf("cc%d - lock for object %i of transaction %i unlocked at time %f \n", pointer->server_id, object_key_id, tx_id, now);

				} else {
					current_entry = pointer->cc_metadata->shared_locks[object_key_id];
					while (current_entry->next != NULL ) {
						if (current_entry->next->tx_id == tx_id) {
							temp_entry = current_entry->next;
							current_entry->next = current_entry->next->next;
							if (pointer->configuration.cc_verbose)
								printf("cc%d - lock for object %i of transaction %i unlocked at time %f \n", pointer->server_id, object_key_id, tx_id, now);

							//Wake up waiting event for key 'object_key_id';
							reschedule_event(state, now, object_key_id);
							free(temp_entry);
							break;
						}
						current_entry = current_entry->next;
					}
				}
			}
			data_entry = data_entry->next;
		} while (data_entry);
	}
}

static void remove_write_locks(int tx_id, data_set_entry *data_set, state_type *state, double now) {

	SERVER_lp_state_type *pointer = &state->type.server_state;

	data_set_entry *de = data_set;
	data_set_entry *entry = data_set;
	int object_id;

	if (de != NULL ) {
		do {
			object_id = de->object_key_id;
			int need_to_unlock = 0;
			if (pointer->configuration.concurrency_control_type == GMU_PRIMARY_OWNER)
				need_to_unlock = is_primary(entry->object_key_id, pointer->server_id, state->num_servers, state->num_clients);
			else
				need_to_unlock = is_owner(object_id, pointer->server_id, state->num_servers, state->num_clients, state->object_replication_degree);
			if (need_to_unlock) {

				if (pointer->cc_metadata->locks[object_id] == tx_id) {
					pointer->cc_metadata->locks[object_id] = -1;
					if (pointer->configuration.cc_verbose)
						printf("cc%d - lock for object %i of transaction %i unlocked at time %f \n", pointer->server_id, object_id, tx_id, now);
					//Wake up waiting event for key 'object_key_id';
					reschedule_event(state, now, object_id);
				} else {
					//printf("ERROR: cc%d - no lock found of transaction id %i on object %i\n", pointer->server_id, tx_id, di_id);
				}
			}
			de = de->next;
		} while (de);
	}
}

static void abort_local_tx(event_content_type * event_content, state_type *state, double now) {
	SERVER_lp_state_type *pointer = &state->type.server_state;
	transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
	if (transaction == NULL ) {
		printf("ERROR: no transaction found with id %i (from client %i)\n", event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
		exit(-1);
	}
	//Remove all events of transaction
	remove_event_of_tx(&pointer->cc_metadata->lock_waiting_event_list, transaction->tx_id);
	remove_write_locks(transaction->tx_id, transaction->write_set, state, now);
	remove_read_locks(transaction->tx_id, transaction->read_set->first, state, now);
	remove_from_commit_queue(event_content->applicative_content.tx_id, state);

}

static void abort_prepare_tx(event_content_type * event_content, state_type *state, double now) {

	SERVER_lp_state_type *pointer = &state->type.server_state;
	//Remove all events of transaction
	remove_event_of_tx(&pointer->cc_metadata->lock_waiting_event_list, event_content->applicative_content.tx_id);
	remove_write_locks(event_content->applicative_content.tx_id, event_content->applicative_content.write_set, state, now);
	remove_read_locks(event_content->applicative_content.tx_id, event_content->applicative_content.read_set, state, now);
}

static void abort_remote_tx(event_content_type * event_content, state_type *state, double now) {

	SERVER_lp_state_type *pointer = &state->type.server_state;
	//Remove all events of transaction
	remove_event_of_tx(&pointer->cc_metadata->lock_waiting_event_list, event_content->applicative_content.tx_id);
	remove_write_locks(event_content->applicative_content.tx_id, event_content->applicative_content.write_set, state, now);
	remove_read_locks(event_content->applicative_content.tx_id, event_content->applicative_content.read_set, state, now);
	remove_from_commit_queue(event_content->applicative_content.tx_id, state);

}

//concurrency control initialization
void concurrency_control_init(state_type *state) {
	SERVER_lp_state_type *pointer = &state->type.server_state;
	int i;
	cc_metadata *cc_met = (cc_metadata *) malloc(sizeof(cc_metadata));
	for (i = 0; i < ACTIVE_TRANSACTION_TABLE_SIZE; i++)
		cc_met->active_transaction[i] = NULL;
	cc_met->locks = malloc(sizeof(int) * state->cache_objects);
	cc_met->lock_waiting_event_list = NULL; // (CC_event_list *) malloc(sizeof(CC_event_list));
	//cc_met->lock_waiting_event_list->event = NULL;
	//cc_met->lock_waiting_event_list->next = NULL;

	memset(cc_met->locks, -1, sizeof(int) * state->cache_objects);

	cc_met->shared_locks = (CC_shared_lock_type**) malloc(sizeof(CC_shared_lock_type*) * state->cache_objects);
	memset(cc_met->shared_locks, 0, sizeof(CC_shared_lock_type*) * state->cache_objects);

	cc_met->lock_retry_num = 0;
	cc_met->commit_log_list = (CC_commit_log_entry*) malloc(sizeof(CC_commit_log_entry));
	cc_met->commit_log_list->concurrency_number = 0;
	cc_met->commit_log_list->commit_vector_clock = (int*) malloc(sizeof(int) * (state->num_servers + 1));
	memset(cc_met->commit_log_list->commit_vector_clock, 0, sizeof(int) * (state->num_servers + 1));
	cc_met->commit_log_list->previous = NULL;
	cc_met->read_waiting_event_list = NULL;
	cc_met->prepare_vector_clock = (int*) malloc(sizeof(int) * (state->num_servers));
	memset(cc_met->prepare_vector_clock, 0, sizeof(int) * (state->num_servers));
	cc_met->commit_queue = NULL;
	cc_met->last_versions = (CC_last_version_entry*) malloc(sizeof(CC_last_version_entry) * state->cache_objects);
	memset(cc_met->last_versions, 0, sizeof(CC_last_version_entry) * state->cache_objects);
	pointer->cc_metadata = cc_met;
}

//enqueue an event
int add_event(CC_event_list ** list, event_content_type * event) {
	event_content_type * new_event = (event_content_type *) malloc(sizeof(event_content_type));
	new_event = (event_content_type *) malloc(sizeof(event_content_type));
	memcpy(new_event, event, sizeof(event_content_type));
	CC_event_list *event_list_entry = (CC_event_list *) malloc(sizeof(CC_event_list));
	event_list_entry->event = new_event;
	event_list_entry->next = NULL;
	if (*list == NULL ) {
		*list = event_list_entry;
	} else {
		CC_event_list *current = *list;
		while (current->next != NULL ) {
			current = current->next;
		}
		current->next = event_list_entry;
	}
	return 1;
}

static int get_waiting_data_item(int locker, int FLAG, SERVER_lp_state_type * pointer) {
	CC_event_list *queue;
	if (FLAG == CC_QUEUE) {
		queue = pointer->cc_metadata->lock_waiting_event_list;
	} else if (FLAG == CC_QUEUE_L1) {
		queue = pointer->cc_metadata->event_queue_L1;
	} else {
		printf("ERROR: flag %d is not valid\n", FLAG);
		return -1;
	}
	if (queue == NULL ) {
		printf("ERROR: queue pointer is null\n");
		return -1;
	}

	while (queue != NULL ) {
		if (queue->event != NULL && queue->event->applicative_content.tx_id == locker && pointer->cc_metadata->locks[queue->event->applicative_content.object_key_id] != locker) {
			return queue->event->applicative_content.object_key_id;
		}
		queue = queue->next;
	}

	return -1;
}

//check waiting cycles
static int check_cycle(int locker, int tx_id, SERVER_lp_state_type * pointer) {
	int object_key_id = get_waiting_data_item(locker, CC_QUEUE, pointer);

	if (object_key_id != -1) { // check if the locker is waiting for another transaction
		// get the transaction which, in turn, is locking the object
		locker = pointer->cc_metadata->locks[object_key_id];
		if (locker == tx_id)
			return 1;
		else
			return 0;
	}
	return 0;
}

static int check_deadlock(event_content_type * event_content, SERVER_lp_state_type * pointer) {
	if (pointer == NULL || event_content == NULL )
		return -1;
	return check_cycle(pointer->cc_metadata->locks[event_content->applicative_content.object_key_id], event_content->applicative_content.tx_id, pointer);
}

int acquire_local_write_locks(state_type *state, data_set_entry *data_set, time_type timeout, int timeout_event_type, event_content_type *event_content, double now) {

	SERVER_lp_state_type *pointer = &state->type.server_state;
	data_set_entry *entry = data_set;
	int one_lock_acquired = 0;
	if (entry == NULL ) {
		if (pointer->configuration.cc_verbose)
			printf("cc%d -  write set of transaction %d is empty\n", pointer->server_id, event_content->applicative_content.tx_id);
		return 2; //no locked acquire
	}
	while (entry != NULL ) {
		int need_to_lock = 0;
		if (pointer->configuration.concurrency_control_type == GMU)
			need_to_lock = is_owner(entry->object_key_id, pointer->server_id, state->num_servers, state->num_clients, state->object_replication_degree);
		else if (pointer->configuration.concurrency_control_type == GMU_PRIMARY_OWNER)
			need_to_lock = is_primary(entry->object_key_id, pointer->server_id, state->num_servers, state->num_clients);
		if (need_to_lock) {
			if (pointer->configuration.cc_verbose)
				printf("cc%d - object %d for transaction %i to be locked\n", pointer->server_id, entry->object_key_id, event_content->applicative_content.tx_id);
			//check lock...
			if (pointer->cc_metadata->locks[entry->object_key_id] == -1 && pointer->cc_metadata->shared_locks[entry->object_key_id] == NULL ) {
				//not locked
				pointer->cc_metadata->lock_retry_num = 0;
				//acquire write lock
				one_lock_acquired++;
				pointer->cc_metadata->locks[entry->object_key_id] = event_content->applicative_content.tx_id;
				if (pointer->configuration.cc_verbose)
					printf("cc%d - object %d  for transaction  %i locked at time %f \n", pointer->server_id, entry->object_key_id, event_content->applicative_content.tx_id, now);
			} else if ((pointer->cc_metadata->locks[entry->object_key_id] == -1) && (pointer->cc_metadata->shared_locks[entry->object_key_id] != NULL )&&
			(pointer->cc_metadata->shared_locks[entry->object_key_id]->tx_id==event_content->applicative_content.tx_id) &&
			(pointer->cc_metadata->shared_locks[entry->object_key_id]->next == NULL)){
			//shared locked by me and there are not other shared locks
			// acquire write lock
					one_lock_acquired++;
					pointer ->cc_metadata->locks[entry->object_key_id] = event_content->applicative_content.tx_id;
				} else if (pointer->cc_metadata->locks[entry->object_key_id] == event_content->applicative_content.tx_id) {
					//already locked by me with a write lock
					one_lock_acquired++;
					//go to the next entry
				} else {
					//already locked by another transaction
					if (pointer->configuration.cc_verbose) {
						if (pointer->cc_metadata->locks[entry->object_key_id] != -1)
						printf("cc%d - object %d is already write locked by transaction %d\n", pointer->server_id, entry->object_key_id, pointer->cc_metadata->locks[entry->object_key_id]);
						else
						printf("cc%d - object %d is already read locked by transaction %d\n", pointer->server_id, entry->object_key_id, pointer->cc_metadata->shared_locks[entry->object_key_id]->tx_id);
					}

					pointer->cc_metadata->lock_retry_num++;
					//check deadlock (if enabled)
					if (pointer->configuration.deadlock_detection_enabled && check_deadlock(event_content, pointer)) {
						return -1;
					}
					//add the timeout event
					ScheduleNewEvent(pointer->server_id, now + timeout, timeout_event_type, event_content, sizeof(event_content_type));
					//enqueue transaction
					event_content->applicative_content.object_key_id = entry->object_key_id;
					add_event(&pointer->cc_metadata->lock_waiting_event_list, event_content);
					transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
					transaction->is_blocked = 1;
					if (pointer->configuration.cc_verbose)
					printf("cc%d - transaction %i is waiting at time %f due to a lock on object %d tx:%i\n", pointer->server_id, event_content->applicative_content.tx_id, now, entry->object_key_id,
							event_content->applicative_content.tx_id);
					return 0;
				}
			}
		entry = entry->next;
	}
	if (one_lock_acquired)
		return 1; //at least one lock has been acquired
	else
		return 2; //no locked acquired
}

static int add_read_lock(int object_key_id, int tx_id, CC_shared_lock_type **object_shared_lock_list) {

	if (*object_shared_lock_list == NULL ) {
		// add lock
		CC_shared_lock_type *new_shared_lock = (CC_shared_lock_type*) malloc(sizeof(CC_shared_lock_type));
		new_shared_lock->tx_id = tx_id;
		new_shared_lock->next = NULL;
		*object_shared_lock_list = new_shared_lock;
		return 1;
	}
	//check only if the first lock is of the same transaction
	if ((*object_shared_lock_list)->tx_id == tx_id)
		return 0;
	// add lock
	CC_shared_lock_type *new_shared_lock = (CC_shared_lock_type*) malloc(sizeof(CC_shared_lock_type));
	new_shared_lock->tx_id = tx_id;
	new_shared_lock->next = *object_shared_lock_list;
	*object_shared_lock_list = new_shared_lock;
	return 1;

}

int acquire_local_read_locks(state_type *state, data_set_entry *data_set, time_type timeout, int timeout_event_type, event_content_type *event_content, double now) {
	SERVER_lp_state_type *pointer = &state->type.server_state;
	data_set_entry *entry = data_set;
	if (entry == NULL ) {
		if (pointer->configuration.cc_verbose)
			printf("cc%d -  read set of transaction %d is empty\n", pointer->server_id, event_content->applicative_content.tx_id);
		return -1;
	}
	while (entry != NULL ) {
		int need_to_lock = 0;
		if (pointer->configuration.concurrency_control_type == GMU)
			need_to_lock = is_owner(entry->object_key_id, pointer->server_id, state->num_servers, state->num_clients, state->object_replication_degree);
		else if (pointer->configuration.concurrency_control_type == GMU_PRIMARY_OWNER)
			need_to_lock = is_primary(entry->object_key_id, pointer->server_id, state->num_servers, state->num_clients);
		if (need_to_lock) {
			if (pointer->configuration.cc_verbose)
				printf("cc%d - object %d for transaction %i to be locked\n", pointer->server_id, entry->object_key_id, event_content->applicative_content.tx_id);
			//check lock...
			if (pointer->cc_metadata->locks[entry->object_key_id] == -1 || pointer->cc_metadata->locks[entry->object_key_id] == event_content->applicative_content.tx_id) {
				//not locked or locked by me
				//add the shared lock
				add_read_lock(entry->object_key_id, event_content->applicative_content.tx_id, &pointer->cc_metadata->shared_locks[entry->object_key_id]);
				if (pointer->configuration.cc_verbose)
					printf("cc%d - object %d  for transaction  %i shared locked at time %f \n", pointer->server_id, entry->object_key_id, event_content->applicative_content.tx_id, now);
			} else {
				//write-locked by another transaction
				//check deadlock (if enabled)
				if (pointer->configuration.deadlock_detection_enabled && check_deadlock(event_content, pointer)) {
					return -1;
				}
				//add the timeout event
				ScheduleNewEvent(pointer->server_id, now + timeout, timeout_event_type, event_content, sizeof(event_content_type));
				//enqueue transaction
				event_content->applicative_content.object_key_id = entry->object_key_id;
				add_event(&pointer->cc_metadata->lock_waiting_event_list, event_content);
				transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
				transaction->is_blocked = 1;
				if (pointer->configuration.cc_verbose)
					printf("cc%d - transaction %i is waiting at time %f due to a lock on object %d tx:%i\n", pointer->server_id, event_content->applicative_content.tx_id, now, entry->object_key_id,
							event_content->applicative_content.tx_id);
				return 0;
			}
		}
		entry = entry->next;
	}
	return 1;
}

int is_in_write_set(int tx_id, int object_key_id, state_type *state) {
	SERVER_lp_state_type *pointer = &state->type.server_state;
	transaction_metadata *transaction = get_transaction_metadata(tx_id, pointer);
	data_set_entry * current_entry = transaction->write_set;
	if (current_entry == NULL )
		return 0;
	while (current_entry->next != NULL ) {
		if (current_entry->object_key_id == object_key_id) {
			return 1;
		}
		current_entry = current_entry->next;
	}
	return 0;
}

static int is_write_transaction(int tx_id, SERVER_lp_state_type * pointer) {
	transaction_metadata * transaction = get_transaction_metadata(tx_id, pointer);
	if (transaction != NULL ) {
		if (transaction->write_set != NULL ) {
			return 1;
		}
	}
	return 0;
}

static int validate_local_read(int tx_id, state_type * state, int object_key_id) {
	CC_last_version_entry last_version = state->type.server_state.cc_metadata->last_versions[object_key_id];
	transaction_metadata * transaction = get_transaction_metadata(tx_id, &(state->type.server_state));
	int my_index = MY_VECTOR_CLOCK_INDEX(state);
	int read_version_number = transaction->read_vector_clock[my_index];
	if (last_version.version_number > read_version_number) { // we don't need subversion number comparison here.
		return 0;
	}
	return 1;

}

static int validate_read_set(data_set_entry * entry, int version, state_type *state, int tx_id, double now) {
	SERVER_lp_state_type *pointer = &state->type.server_state;
	while (entry != NULL ) {
		if (is_owner(entry->object_key_id, pointer->server_id, state->num_servers, state->num_clients, state->object_replication_degree)) {
			if (pointer->cc_metadata->last_versions[entry->object_key_id].version_number > version) {
				return -1;
			}
		}
		entry = entry->next;
	}
	return 1;
}

static int validate_remote_read(int tx_id, state_type * state, int object_key_id, int* vector_clock_bound, CC_last_version_entry* forbidden_list) {

	int vector_clock_index = MY_VECTOR_CLOCK_INDEX(state);
	CC_last_version_entry last_version = state->type.server_state.cc_metadata->last_versions[object_key_id];
	CC_last_version_entry* current_forbidden = forbidden_list;

	if (vector_clock_bound[vector_clock_index] < last_version.version_number) {
		return 0;
	}
	while (current_forbidden != NULL ) {
		if (last_version.version_number == current_forbidden->version_number && last_version.subversion_number == current_forbidden->subversion_number) {
			return 0;
		}
		current_forbidden = current_forbidden->next;
	}
	return 1;
}

static void get_read_boundaries(int tx_id, state_type * state, int* read_vector_clock, short* has_read_mask, int** vector_clock_bound_pointer, CC_last_version_entry** forbidden_list_pointer) {

	int vector_clock_index = MY_VECTOR_CLOCK_INDEX(state);
	*vector_clock_bound_pointer = (int*) malloc(sizeof(int) * state->num_servers);

	CC_commit_log_entry *commit_log = state->type.server_state.cc_metadata->commit_log_list;

	*forbidden_list_pointer = NULL;

	CC_last_version_entry *current_forbidden = NULL;
	CC_last_version_entry *prev_forbidden = NULL;

	int i;
	int* upper_bound = NULL;
	int set_upper_bound = 1;
	int * current_commit_vector_clock;

	int concurrency_number_bound = 0;
	while (commit_log != NULL && (upper_bound == NULL || concurrency_number_bound < commit_log->commit_vector_clock[vector_clock_index])) {
		current_commit_vector_clock = commit_log->commit_vector_clock;
		set_upper_bound = 1;

		for (i = 0; i < state->num_servers; i++) {
			if (has_read_mask[i] != 0) {
				if (current_commit_vector_clock[i] > read_vector_clock[i]) {
					set_upper_bound = 0;

					//Insert in the forbidden list if upper_bound is different from NULL
					if (upper_bound != NULL ) {
						current_forbidden = (CC_last_version_entry*) malloc(sizeof(CC_last_version_entry));
						current_forbidden->version_number = current_commit_vector_clock[vector_clock_index];
						current_forbidden->subversion_number = current_commit_vector_clock[state->num_servers]; //it's ok. The size is numServers+1
						current_forbidden->next = NULL;

						if (*forbidden_list_pointer == NULL ) {
							*forbidden_list_pointer = current_forbidden;
							prev_forbidden = current_forbidden;
						} else {
							prev_forbidden->next = current_forbidden;
							prev_forbidden = current_forbidden;
						}
					}
					break;
				}
			}
		}

		if (upper_bound == NULL && set_upper_bound != 0) {
			upper_bound = current_commit_vector_clock;
			memcpy(*vector_clock_bound_pointer, upper_bound, state->num_servers * sizeof(int)); //Only numServers it's ok.
			concurrency_number_bound = commit_log->concurrency_number;
		} else if (upper_bound != NULL && set_upper_bound != 0) {

			for (i = 0; i < state->num_servers; i++) {

				//The final vector clock bound is the max among the upper bound and all the other concurrent commit vector clocks
				if ((*vector_clock_bound_pointer)[i] < current_commit_vector_clock[i]) {
					(*vector_clock_bound_pointer)[i] = current_commit_vector_clock[i];
				}

			}
		}

		if (upper_bound != NULL && concurrency_number_bound > commit_log->concurrency_number) {
			//We move the bound in the past
			concurrency_number_bound = commit_log->concurrency_number;
		}

		commit_log = commit_log->previous;
	}

}

static void free_cc_last_version_list(CC_last_version_entry* list) {
	CC_last_version_entry * to_delete;
	while (list != NULL ) {
		to_delete = list;
		list = list->next;
		free(to_delete);
	}
}

static void update_transaction_metadata_on_remote_get_return(state_type *state, transaction_metadata *transaction, event_content_type *event_content) {

	int i;

	int sender = event_content->origin_object_id;
	int sender_index = VECTOR_CLOCK_INDEX(state,sender);

	//Update the reading vector clock
	for (i = 0; i < state->num_servers; i++) {
		if (transaction->read_vector_clock[i] < event_content->applicative_content.read_vector_clock[i]) {
			(transaction->read_vector_clock)[i] = event_content->applicative_content.read_vector_clock[i];
		}
	}

	transaction->has_read_mask[sender_index] = 1;
	free(event_content->applicative_content.read_vector_clock);

}

static inline void increment_and_store_prepare_vector_clock(int* vector_clock, state_type * state) {
	(state->type.server_state.cc_metadata->prepare_vector_clock)[MY_VECTOR_CLOCK_INDEX(state)]++;
	memcpy(vector_clock, (state->type.server_state.cc_metadata->prepare_vector_clock), sizeof(int) * state->num_servers);
}

static inline void store_prepare_vector_clock(int* vector_clock, state_type * state) {
	memcpy(vector_clock, (state->type.server_state.cc_metadata->prepare_vector_clock), sizeof(int) * state->num_servers);
}

static void merge_vector_clocks(int *v1, int *v2, int size) {
// select the maximum for each entry and update v1
	int i;
	for (i = 0; i < size; i++) {
		if (v1[i] < v2[i])
			v1[i] = v2[i];
	}
}

static CC_commit_queue_entry *create_commit_queue_entry_for_local_transaction(transaction_metadata *transaction, state_type* state) {

	CC_commit_log_entry * last_committed = state->type.server_state.cc_metadata->commit_log_list;

	int my_index = MY_VECTOR_CLOCK_INDEX(state);

	CC_commit_queue_entry * new_entry = (CC_commit_queue_entry*) malloc(sizeof(CC_commit_queue_entry));
	new_entry->tx_id = transaction->tx_id;
	new_entry->concurrency_number = last_committed->commit_vector_clock[my_index];
	new_entry->read_set = transaction->read_set->first;
	new_entry->write_set = transaction->write_set;
	new_entry->ready_to_commit = 0;
	new_entry->vector_clock = (int*) malloc(sizeof(int) * state->num_servers);
	memcpy(new_entry->vector_clock, transaction->commit_vector_clock, sizeof(int) * state->num_servers);
	new_entry->next = NULL;
	return new_entry;
}

static CC_commit_queue_entry *create_commit_queue_entry_for_remote_transaction(event_content_type* event_content, int* vector_clock, state_type* state) {

	CC_commit_log_entry * last_committed = state->type.server_state.cc_metadata->commit_log_list;

	int my_index = MY_VECTOR_CLOCK_INDEX(state);

	CC_commit_queue_entry * new_entry = (CC_commit_queue_entry*) malloc(sizeof(CC_commit_queue_entry));
	new_entry->tx_id = event_content->applicative_content.tx_id;
	new_entry->concurrency_number = last_committed->commit_vector_clock[my_index];
	new_entry->read_set = event_content->applicative_content.read_set;
	new_entry->write_set = event_content->applicative_content.write_set;
	new_entry->ready_to_commit = 0;
	//new_entry->vector_clock = (int*) malloc(sizeof(int) * state->num_servers);
	new_entry->vector_clock = vector_clock;
	//memcpy(new_entry->vector_clock, transaction->commit_vector_clock, sizeof(int) * state->num_servers);
	new_entry->next = NULL;
	return new_entry;
}

static void add_to_commit_queue(CC_commit_queue_entry * new_entry, state_type* state) {

	if (state->type.server_state.cc_metadata->commit_queue == NULL ) {
		state->type.server_state.cc_metadata->commit_queue = new_entry;
	} else {
		int index = MY_VECTOR_CLOCK_INDEX(state);
		CC_commit_queue_entry *temp_entry, *current_entry;
		if (state->type.server_state.cc_metadata->commit_queue->vector_clock[index] > new_entry->vector_clock[index]) {
			temp_entry = state->type.server_state.cc_metadata->commit_queue;
			state->type.server_state.cc_metadata->commit_queue = new_entry;
			new_entry->next = temp_entry;
		} else {
			current_entry = state->type.server_state.cc_metadata->commit_queue;
			while (current_entry->next != NULL && current_entry->next->vector_clock[index] < new_entry->vector_clock[index]) {
				current_entry = current_entry->next;
			}
			temp_entry = current_entry->next;
			current_entry->next = new_entry;
			new_entry->next = temp_entry;
		}
	}
}

int update_commit_queue(int tx_id, int * commitVectorClock, state_type* state) {

	CC_commit_queue_entry * new_entry;
	if (state->type.server_state.cc_metadata->commit_queue == NULL ) {
		return 0;
	} else {
		CC_commit_queue_entry *current_entry;
		if (state->type.server_state.cc_metadata->commit_queue->tx_id == tx_id) {
			new_entry = state->type.server_state.cc_metadata->commit_queue;
			state->type.server_state.cc_metadata->commit_queue = new_entry->next;
		} else {
			current_entry = state->type.server_state.cc_metadata->commit_queue;
			while (current_entry->next != NULL && current_entry->next->tx_id != tx_id) {
				current_entry = current_entry->next;
			}
			if (current_entry->next == NULL )
				return 0;

			new_entry = current_entry->next;
			current_entry->next = new_entry->next;
		}

		memcpy(new_entry->vector_clock, commitVectorClock, sizeof(int) * state->num_servers);
		new_entry->ready_to_commit = 1;
		new_entry->next = NULL;
		add_to_commit_queue(new_entry, state);
		return 1;
	}
}

int remove_from_commit_queue(int tx_id, state_type* state) {

	CC_commit_queue_entry * to_delete;
	if (state->type.server_state.cc_metadata->commit_queue == NULL ) {
		return 0;
	} else {
		CC_commit_queue_entry *current_entry;
		if (state->type.server_state.cc_metadata->commit_queue->tx_id == tx_id) {
			to_delete = state->type.server_state.cc_metadata->commit_queue;
			state->type.server_state.cc_metadata->commit_queue = to_delete->next;
		} else {
			current_entry = state->type.server_state.cc_metadata->commit_queue;
			while (current_entry->next != NULL && current_entry->next->tx_id != tx_id) {
				current_entry = current_entry->next;
			}
			if (current_entry->next == NULL )
				return 0;

			to_delete = current_entry->next;
			current_entry->next = to_delete->next;
		}
		if (!to_delete) {
			free(to_delete);
			return 1;
		} else {
			return 0;
		}
	}
}

static void awake_reads_after_commit(double now, state_type* state) {

	SERVER_lp_state_type * pointer = &(state->type.server_state);

	CC_event_list* entry_to_be_removed;
	int my_index = MY_VECTOR_CLOCK_INDEX(state);
	CC_event_list* current_entry;

	while ((pointer->cc_metadata->read_waiting_event_list != NULL )&& (pointer->cc_metadata->read_waiting_event_list->event->applicative_content.read_vector_clock[my_index] <= pointer->cc_metadata->commit_log_list->commit_vector_clock[my_index]) ){
	//we can remove current node from the queue and wake up the associated event
	entry_to_be_removed = pointer->cc_metadata->read_waiting_event_list;
	pointer->cc_metadata->read_waiting_event_list = pointer->cc_metadata->read_waiting_event_list->next;
	ScheduleNewEvent(pointer->server_id, now, DELIVER_MESSAGE, entry_to_be_removed->event, sizeof(event_content_type));
	free(entry_to_be_removed->event);
	free(entry_to_be_removed);
}

	if (pointer->cc_metadata->read_waiting_event_list != NULL ) {
		current_entry = pointer->cc_metadata->read_waiting_event_list;
		while (current_entry->next != NULL ) {
			if (current_entry->next->event->applicative_content.read_vector_clock[my_index] <= pointer->cc_metadata->commit_log_list->commit_vector_clock[my_index]) {
				//we can remove current node from the queue and wake up the associated event
				entry_to_be_removed = current_entry->next;
				current_entry->next = current_entry->next->next;
				ScheduleNewEvent(pointer->server_id, now, DELIVER_MESSAGE, entry_to_be_removed->event, sizeof(event_content_type));
				free(entry_to_be_removed->event);
				free(entry_to_be_removed);
			} else {
				current_entry = current_entry->next;
			}
		}
	}
}

static void add_vector_clock_to_commit_log(double now, int* vector_clock, int vector_clock_size, state_type * state, int subversion, int concurrency_number) {

	SERVER_lp_state_type * pointer = &(state->type.server_state);

	CC_commit_log_entry *new_entry = (CC_commit_log_entry*) malloc(sizeof(CC_commit_log_entry));
	new_entry->concurrency_number = concurrency_number;
	new_entry->commit_vector_clock = (int*) malloc(sizeof(int) * (vector_clock_size + 1));
	memcpy(new_entry->commit_vector_clock, vector_clock, vector_clock_size * sizeof(int));

	new_entry->commit_vector_clock[vector_clock_size] = subversion;

	new_entry->previous = pointer->cc_metadata->commit_log_list;
	pointer->cc_metadata->commit_log_list = new_entry;

	awake_reads_after_commit(now, state);

}

static int try_to_commit_queued_transactions(double now, state_type* state) {

	int num_try_to_commit = 0, can_commit = 0, i;
	int my_index = MY_VECTOR_CLOCK_INDEX(state);
	CC_commit_queue_entry *current = state->type.server_state.cc_metadata->commit_queue;

	while (current != NULL ) {
		if ((current->ready_to_commit)) {
			num_try_to_commit++;
			if (current->next == NULL ) {
				can_commit = 1;
				break;
			} else if (current->next->vector_clock[my_index] > current->vector_clock[my_index]) {
				can_commit = 1;
				break;
			}
		} else {
			break;
		}
		current = current->next;
	}

	if (can_commit != 0) {

		for (i = 0; i < num_try_to_commit; i++) {
			current = state->type.server_state.cc_metadata->commit_queue;
			state->type.server_state.cc_metadata->commit_queue = current->next;

			//update objects
			data_set_entry * current_write_set_entry = current->write_set;
			while (current_write_set_entry) {
				if (is_owner(current_write_set_entry->object_key_id, state->type.server_state.server_id, state->num_servers, state->num_clients, state->object_replication_degree)) {
					(state->type.server_state.cc_metadata->last_versions)[current_write_set_entry->object_key_id].version_number = (current->vector_clock)[my_index];
					(state->type.server_state.cc_metadata->last_versions)[current_write_set_entry->object_key_id].subversion_number = i;

					if (state->type.server_state.configuration.cc_verbose) {
//						printf("\tCC%d - commit object %d - versions=(%d, %d)\n", state->type.server_state.server_id, (current->writeSet)[j], (current->vector_clock_)[my_index], i);
					}
				}
				current_write_set_entry = current_write_set_entry->next;
			}

			//Add commit vector clock in commit log
			add_vector_clock_to_commit_log(now, current->vector_clock, state->num_servers, state, i, current->concurrency_number);

			//Remove locks
			remove_read_locks(current->tx_id, current->read_set, state, now);
			remove_write_locks(current->tx_id, current->write_set, state, now);

			//Delete current node
			if (current->vector_clock != NULL ) {
				free(current->vector_clock);
			}
			free(current);
		}

		try_to_commit_queued_transactions(now, state);
	}

	return can_commit;
}

//main concurrency control function: return 0 if the event is enqueued, 1 if the accessing object is locked, -1 if transaction must be aborted
int CC_control(event_content_type * event_content, state_type *state, time_type now) {
	SERVER_lp_state_type *pointer = &state->type.server_state;
	if (pointer == NULL || event_content == NULL )
		return -1;

	transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
	int acquire_local_read_locks_return_value = 0;
	int acquire_local_write_locks_return_value = 0;
	int found;

	switch (event_content->applicative_content.op_type) {

	case TX_BEGIN:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_BEGIN\n", pointer->server_id);
		if (event_content->applicative_content.tx_id == 1114)
			found = 0;
		int vector_clock_index = MY_VECTOR_CLOCK_INDEX(state);

		if (transaction->read_vector_clock != NULL )
			free(transaction->read_vector_clock);

		transaction->read_vector_clock = (int*) malloc(sizeof(int) * state->num_servers);
		memcpy(transaction->read_vector_clock, state->type.server_state.cc_metadata->commit_log_list->commit_vector_clock, sizeof(int) * state->num_servers);
		if (transaction->commit_vector_clock != NULL )
			free(transaction->commit_vector_clock);

		transaction->commit_vector_clock = (int*) malloc(sizeof(int) * state->num_servers);
		memset(transaction->commit_vector_clock, 0, sizeof(int) * state->num_servers);

		if (transaction->has_read_mask != NULL )
			free(transaction->has_read_mask);
		transaction->has_read_mask = (short*) malloc(sizeof(short) * state->num_servers);
		memset(transaction->has_read_mask, 0, sizeof(short) * state->num_servers);
		transaction->has_read_mask[vector_clock_index] = 1;

		if (transaction->nodes_on_write_mask != NULL )
			free(transaction->nodes_on_write_mask);
		transaction->nodes_on_write_mask = (short*) malloc(sizeof(short) * state->num_servers);
		memset(transaction->nodes_on_write_mask, 0, sizeof(short) * state->num_servers);
		return 1;
		break;

	case TX_GET:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_GET for tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);

		if (event_content->applicative_content.tx_id == 1114 && event_content->applicative_content.op_number == 8)
			found = 0;
		if (is_write_transaction(event_content->applicative_content.tx_id, pointer)) {
			if (!validate_local_read(event_content->applicative_content.tx_id, state, event_content->applicative_content.object_key_id)) {
				// non actions needed because locks have not been acquired
				abort_local_tx(event_content, state, now);
				return -1;
			}
		}

		int return_from_add_data_to_read_set = add_data_to_read_set(transaction, event_content->applicative_content.client_id, pointer, event_content->applicative_content.object_key_id);
		if (return_from_add_data_to_read_set == -1) {
			// non actions needed because locks have not been acquired
			if (pointer->configuration.cc_verbose)
				printf("\tcc%d - ERROR while adding object %i in the read-set of transaction %d from client %d\n", pointer->server_id, event_content->applicative_content.object_key_id,
						event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
			return -1;
		} else {
			if (return_from_add_data_to_read_set == 0 && pointer->configuration.cc_verbose)
				printf("\tcc%d - object %d is already in the read-set of transaction %d from client %d\n", pointer->server_id, event_content->applicative_content.object_key_id,
						event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
			else if (pointer->configuration.cc_verbose)
				printf("\tcc%d - object %d added to the read-set of transaction tx %d\n", pointer->server_id, event_content->applicative_content.object_key_id,
						event_content->applicative_content.tx_id);
		}
		return 1;
		break;

	case TX_REMOTE_GET: //get received from a server

		if (pointer->configuration.server_verbose)
			printf("\tcc%d - TX_REMOTE_GET for tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);
		int index = MY_VECTOR_CLOCK_INDEX(state);
		if (event_content->applicative_content.read_vector_clock[index] > pointer->cc_metadata->commit_log_list->commit_vector_clock[index]) {
			//need to wait
			add_event(&pointer->cc_metadata->read_waiting_event_list, event_content);
			if (pointer->configuration.server_verbose)
				printf("\tcc%d - TX_READ op num %i from tx %i must wait\n", pointer->server_id, event_content->applicative_content.op_number, event_content->applicative_content.tx_id);
			return 0;
		}
		int* vector_clock_boudary_pointer;
		CC_last_version_entry *forbidden_list_pointer = NULL;
		get_read_boundaries(event_content->applicative_content.tx_id, state, event_content->applicative_content.read_vector_clock, event_content->applicative_content.has_read_mask,
				&vector_clock_boudary_pointer, &forbidden_list_pointer);
		int is_valid = validate_remote_read(event_content->applicative_content.tx_id, state, event_content->applicative_content.object_key_id, vector_clock_boudary_pointer, forbidden_list_pointer);
		//Add to the event with the new vector_clock_bound and the validation outcome
		memcpy(event_content->applicative_content.read_vector_clock, vector_clock_boudary_pointer, sizeof(int) * state->num_servers);
		event_content->applicative_content.valid_remote_read = is_valid;
		free_cc_last_version_list(forbidden_list_pointer);
		return 1;
		break;

	case TX_REMOTE_GET_RETURN:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_REMOTE_GET_RETURN for tx %i - object %d\n", pointer->server_id, event_content->applicative_content.tx_id, event_content->applicative_content.object_key_id);

		transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		if (transaction->write_set != NULL && !event_content->applicative_content.valid_remote_read) {
			// non actions needed because locks have not been acquired
			abort_local_tx(event_content, state, now);
			return -1;
		}
		update_transaction_metadata_on_remote_get_return(state, transaction, event_content);
		return_from_add_data_to_read_set = add_data_to_read_set(transaction, event_content->applicative_content.client_id, pointer, event_content->applicative_content.object_key_id);
		if (return_from_add_data_to_read_set == -1) {
			// non actions needed because locks have not been acquired
			return -1;
		}

		if (return_from_add_data_to_read_set == 0 && pointer->configuration.cc_verbose)
			printf("\tcc%d - object with id %d is already included in the read set of tx %d of client %d\n", pointer->server_id, event_content->applicative_content.object_key_id,
					event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
		else if (pointer->configuration.cc_verbose)
			printf("\tcc%d - object with id %d added to read-set of tx %d\n", pointer->server_id, event_content->applicative_content.object_key_id, event_content->applicative_content.tx_id);

		return 1;

		break;

	case TX_PUT:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d -  TX_PUT for tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);
		//add data to write-set
		add_data_to_write_set(event_content->applicative_content.tx_id, event_content->applicative_content.client_id, pointer, event_content->applicative_content.object_key_id);

		return 1;
		break;

	case TX_COMMIT: // commit request received from a client

		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_COMMIT for tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);

		transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		if (transaction == NULL ) {
			printf("ERROR: no transaction found with id %d (from client id %d)\n", event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
			exit(-1);
		}

		if (transaction->write_set != NULL ) {

			acquire_local_write_locks_return_value = acquire_local_write_locks(state, transaction->write_set, pointer->configuration.locking_timeout, TX_LOCAL_TIMEOUT, event_content, now);

			if (acquire_local_write_locks_return_value == -1) {
				abort_local_tx(event_content, state, now);
				return -1;
			} else if (acquire_local_write_locks_return_value == 0) {

				return 0;
			}
			if (transaction->read_set->first != NULL ) {
				acquire_local_read_locks_return_value = acquire_local_read_locks(state, transaction->read_set->first, pointer->configuration.locking_timeout, TX_LOCAL_TIMEOUT, event_content, now);
				if (acquire_local_read_locks_return_value == -1) {

					abort_local_tx(event_content, state, now);
					return -1;
				} else if (acquire_local_read_locks_return_value == 0) {

					return 0;
				}
			}
			if (!validate_read_set(transaction->read_set->first, transaction->read_vector_clock[MY_VECTOR_CLOCK_INDEX(state)], state, transaction->tx_id, now)) {

				abort_local_tx(event_content, state, now);
				return -1;
			}

			if (acquire_local_write_locks_return_value == 1) { //transaction has to write on this node
				increment_and_store_prepare_vector_clock(transaction->commit_vector_clock, state);
				add_to_commit_queue(create_commit_queue_entry_for_local_transaction(transaction, state), state);
			} else {
				store_prepare_vector_clock(transaction->commit_vector_clock, state);
			}

		}
		return 1;
		break;

	case TX_PREPARE: //prepare request received from a coordinator server

		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_PREPARE for tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);

		data_set_entry *ds_entry = event_content->applicative_content.write_set;
		if (ds_entry != NULL ) {
			acquire_local_write_locks_return_value = acquire_local_write_locks(state, ds_entry, pointer->configuration.locking_timeout, TX_PREPARE_TIMEOUT, event_content, now);
			if (acquire_local_write_locks_return_value == -1) {
				abort_remote_tx(event_content, state, now);
				return -1;
			} else if (acquire_local_write_locks_return_value == 0) {
				return 0;
			}
		}

		ds_entry = event_content->applicative_content.read_set;
		if (ds_entry != NULL ) {
			acquire_local_read_locks_return_value = acquire_local_read_locks(state, ds_entry, pointer->configuration.locking_timeout, TX_PREPARE_TIMEOUT, event_content, now);
			if (acquire_local_read_locks_return_value == -1) {

				abort_remote_tx(event_content, state, now);
				return -1;
			} else if (acquire_local_read_locks_return_value == 0) {
				return 0;
			}
		}

		if (!validate_read_set(event_content->applicative_content.read_set, event_content->applicative_content.read_vector_clock[MY_VECTOR_CLOCK_INDEX(state)], state,
				event_content->applicative_content.tx_id, now)) {
			abort_local_tx(event_content, state, now);
			return -1;
		}

		if (acquire_local_write_locks_return_value == 1) { //transaction has to write on this node
			increment_and_store_prepare_vector_clock(transaction->commit_vector_clock, state);
			add_to_commit_queue(create_commit_queue_entry_for_remote_transaction(event_content, transaction->commit_vector_clock, state), state);
		} else {
			store_prepare_vector_clock(transaction->commit_vector_clock, state);
		}
		event_content->applicative_content.commit_vector_clock = transaction->commit_vector_clock;
		return 1;
		break;

	case TX_PREPARE_SUCCEEDED: // received from a remote participant server

		transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		merge_vector_clocks(transaction->commit_vector_clock, event_content->applicative_content.commit_vector_clock, state->num_servers);

		if (transaction->expected_prepare_response_counter == 0) {

			int max = 0, i;

			for (i = 0; i < state->num_servers; i++) {
				if (transaction->commit_vector_clock[i] > max) {
					max = transaction->commit_vector_clock[i];
				}
			}

			for (i = 0; i < state->num_servers; i++) {
				if (transaction->nodes_on_write_mask[i] != 0) {
					transaction->commit_vector_clock[i] = max;
				}
			}
		}
		return 1;

	case TX_FINAL_LOCAL_COMMIT:

		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_FINAL_LOCAL_COMMIT for tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);
		if (transaction == NULL ) {
			printf("ERROR: no transaction found with id %d (from client id %d)\n", event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
			exit(-1);
		}

		transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		merge_vector_clocks(pointer->cc_metadata->prepare_vector_clock, transaction->commit_vector_clock, state->num_servers);
		found = update_commit_queue(transaction->tx_id, transaction->commit_vector_clock, state);

		if (!found) { //This transaction does not want to write on this server
			remove_read_locks(transaction->tx_id, transaction->read_set->first, state, now);
		} else {

			int commit = try_to_commit_queued_transactions(now, state);
			if (commit) {
				//GMU_GC(state);//TODO uncomment
			}
		}

		return 1;
		break;

	case TX_FINAL_REMOTE_COMMIT:

		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_FINAL_REMOTE_COMMIT for tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);

		transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		merge_vector_clocks(pointer->cc_metadata->prepare_vector_clock, event_content->applicative_content.commit_vector_clock, state->num_servers);
		found = update_commit_queue(event_content->applicative_content.tx_id, event_content->applicative_content.commit_vector_clock, state);

		if (!found) { //This transaction does not want to write on this server
			remove_read_locks(event_content->applicative_content.tx_id, event_content->applicative_content.read_set, state, now);
		} else {
			int commit = try_to_commit_queued_transactions(now, state);
			if (commit) {
				//GMU_GC(state);//TODO uncomment
			}
		}
		return 1;
		break;

	case TX_LOCAL_ABORT:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_ABORT  for tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);
		abort_local_tx(event_content, state, now);
		int commit = try_to_commit_queued_transactions(now, state);
		if (commit) {
			//GMU_GC(state);//TODO uncomment
		}
		return -1;
		break;

	case TX_PREPARE_ABORT:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_PREPARE_ABORT  for tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);

		abort_prepare_tx(event_content, state, now);

		return -1;
		break;

	case TX_REMOTE_ABORT:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_ABORT  for tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);

		abort_remote_tx(event_content, state, now);
		commit = try_to_commit_queued_transactions(now, state);
		if (commit) {
			//GMU_GC(state);//TODO uncomment
		}
		return -1;
		break;

	default:
		printf("ERROR: operation type %i not managed by cc\n", event_content->applicative_content.op_type);
		exit(-1);
	}
}

