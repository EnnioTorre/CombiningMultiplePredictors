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
#ifndef _TRANSACTIONAL_LIST_MANAGER_H
#define _TRANSACTIONAL_LIST_MANAGER_H

#include <stdio.h>
#include "descriptors.h"

double get_tpcc_inter_tx_operation_think_time(int tx_class_id);

transaction_descriptor *create_new_tpcc_transaction(state_type *state, int tx_id);

// crea una transazione e la accoda
transaction_descriptor *create_new_syntetic_transaction(state_type *state, int tx_id, double data_items_zipf_const);

//restituisce la transazione tx_id 
transaction_descriptor *get_transaction(int tx_id, transaction_descriptor * tx_list);

// rimuove una transazione dalla lista
void remove_transaction(int tx_id, transaction_descriptor ** tx_list);

// resetta i puntatori current della transazione
void reset_transaction_state(transaction_descriptor * transaction);

// restituisce l'operazione corrente della transazione e passa alla successiva
operation_descriptor *get_next_operation(transaction_descriptor * tx_desc);

#endif /* _TRANSACTIONAL_LIST_MANAGER_H */
