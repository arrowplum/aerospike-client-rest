/*
 * WARNING: DO NOT EDIT THIS FILE. This file is generated by yarn gen. Any changes will be overwritten.
 */

import { ActionWithPayload } from './Action';
import { Action } from 'redux';
import { RestClientError } from '../api';
import SimpleResponse from '../types/SimpleResponse';

export const TRUNCATE_NAMESPACE_SUCCESSFUL = 'TRUNCATE_NAMESPACE_SUCCESSFUL';
export type TruncateNamespaceSuccessful = ActionWithPayload<
    typeof TRUNCATE_NAMESPACE_SUCCESSFUL,
    SimpleResponse
>;

export const truncateNamespaceSuccessful = (res: SimpleResponse): TruncateNamespaceSuccessful => ({
    type: TRUNCATE_NAMESPACE_SUCCESSFUL,
    payload: res,
});

export const TRUNCATE_NAMESPACE_FAILED = 'TRUNCATE_NAMESPACE_FAILED';
export type TruncateNamespaceFailed = ActionWithPayload<
    typeof TRUNCATE_NAMESPACE_FAILED,
    RestClientError
>;

export const truncateNamespaceFailed = (res: RestClientError): TruncateNamespaceFailed => ({
    type: TRUNCATE_NAMESPACE_FAILED,
    payload: res,
});

export const TRUNCATE_NAMESPACE_IN_PROGRESS = 'TRUNCATE_NAMESPACE_IN_PROGRESS';
export type TruncateNamespaceInProgress = Action<typeof TRUNCATE_NAMESPACE_IN_PROGRESS>;

export const truncateNamespaceInProgress = (): TruncateNamespaceInProgress => ({
    type: TRUNCATE_NAMESPACE_IN_PROGRESS,
});

export type TruncateNamespaceAction =
    | TruncateNamespaceSuccessful
    | TruncateNamespaceFailed
    | TruncateNamespaceInProgress;

export const TRUNCATE_SET_SUCCESSFUL = 'TRUNCATE_SET_SUCCESSFUL';
export type TruncateSetSuccessful = ActionWithPayload<
    typeof TRUNCATE_SET_SUCCESSFUL,
    SimpleResponse
>;

export const truncateSetSuccessful = (res: SimpleResponse): TruncateSetSuccessful => ({
    type: TRUNCATE_SET_SUCCESSFUL,
    payload: res,
});

export const TRUNCATE_SET_FAILED = 'TRUNCATE_SET_FAILED';
export type TruncateSetFailed = ActionWithPayload<typeof TRUNCATE_SET_FAILED, RestClientError>;

export const truncateSetFailed = (res: RestClientError): TruncateSetFailed => ({
    type: TRUNCATE_SET_FAILED,
    payload: res,
});

export const TRUNCATE_SET_IN_PROGRESS = 'TRUNCATE_SET_IN_PROGRESS';
export type TruncateSetInProgress = Action<typeof TRUNCATE_SET_IN_PROGRESS>;

export const truncateSetInProgress = (): TruncateSetInProgress => ({
    type: TRUNCATE_SET_IN_PROGRESS,
});

export type TruncateSetAction = TruncateSetSuccessful | TruncateSetFailed | TruncateSetInProgress;

export type TruncateOperationsAction = TruncateNamespaceAction | TruncateSetAction;