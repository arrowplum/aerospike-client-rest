/*
 * WARNING: DO NOT EDIT THIS FILE. This file is generated by yarn gen. Any changes will be overwritten.
 */

import { Action } from 'redux';
import { ApiState } from '../state/ApiState';
import { createTypedMap } from '../../../core/types/TypedMap';

import adminOperations from './adminOperationsReducer';
import batchReadOperations from './batchReadOperationsReducer';
import clusterInformationOperations from './clusterInformationOperationsReducer';
import secondaryIndexMethods from './secondaryIndexMethodsReducer';
import infoOperations from './infoOperationsReducer';
import keyValueOperations from './keyValueOperationsReducer';
import operateOperations from './operateOperationsReducer';
import truncateOperations from './truncateOperationsReducer';

export default function apiReducer(state: ApiState | undefined, action: Action) {
    if (!state) {
        state = createTypedMap();
    }
    return [
        adminOperations,
        batchReadOperations,
        clusterInformationOperations,
        secondaryIndexMethods,
        infoOperations,
        keyValueOperations,
        operateOperations,
        truncateOperations,
    ].reduce(
        (state, reducer: (state: ApiState, action: Action) => ApiState) => reducer(state, action),
        state
    );
}