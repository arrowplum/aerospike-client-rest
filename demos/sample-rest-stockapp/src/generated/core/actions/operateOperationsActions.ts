/*
 * WARNING: DO NOT EDIT THIS FILE. This file is generated by yarn gen. Any changes will be overwritten.
 */

import { ActionWithPayload } from './Action';
import { Action } from 'redux';
import { Record, RestClientError } from '../api';

export const OPERATE_NAMESPACE_KEY_SUCCESSFUL = 'OPERATE_NAMESPACE_KEY_SUCCESSFUL';
export type OperateNamespaceKeySuccessful = ActionWithPayload<
    typeof OPERATE_NAMESPACE_KEY_SUCCESSFUL,
    Record
>;

export const operateNamespaceKeySuccessful = (res: Record): OperateNamespaceKeySuccessful => ({
    type: OPERATE_NAMESPACE_KEY_SUCCESSFUL,
    payload: res,
});

export const OPERATE_NAMESPACE_KEY_FAILED = 'OPERATE_NAMESPACE_KEY_FAILED';
export type OperateNamespaceKeyFailed = ActionWithPayload<
    typeof OPERATE_NAMESPACE_KEY_FAILED,
    RestClientError
>;

export const operateNamespaceKeyFailed = (res: RestClientError): OperateNamespaceKeyFailed => ({
    type: OPERATE_NAMESPACE_KEY_FAILED,
    payload: res,
});

export const OPERATE_NAMESPACE_KEY_IN_PROGRESS = 'OPERATE_NAMESPACE_KEY_IN_PROGRESS';
export type OperateNamespaceKeyInProgress = Action<typeof OPERATE_NAMESPACE_KEY_IN_PROGRESS>;

export const operateNamespaceKeyInProgress = (): OperateNamespaceKeyInProgress => ({
    type: OPERATE_NAMESPACE_KEY_IN_PROGRESS,
});

export type OperateNamespaceKeyAction =
    | OperateNamespaceKeySuccessful
    | OperateNamespaceKeyFailed
    | OperateNamespaceKeyInProgress;

export const OPERATE_NAMESPACE_SET_KEY_SUCCESSFUL = 'OPERATE_NAMESPACE_SET_KEY_SUCCESSFUL';
export type OperateNamespaceSetKeySuccessful = ActionWithPayload<
    typeof OPERATE_NAMESPACE_SET_KEY_SUCCESSFUL,
    Record
>;

export const operateNamespaceSetKeySuccessful = (
    res: Record
): OperateNamespaceSetKeySuccessful => ({
    type: OPERATE_NAMESPACE_SET_KEY_SUCCESSFUL,
    payload: res,
});

export const OPERATE_NAMESPACE_SET_KEY_FAILED = 'OPERATE_NAMESPACE_SET_KEY_FAILED';
export type OperateNamespaceSetKeyFailed = ActionWithPayload<
    typeof OPERATE_NAMESPACE_SET_KEY_FAILED,
    RestClientError
>;

export const operateNamespaceSetKeyFailed = (
    res: RestClientError
): OperateNamespaceSetKeyFailed => ({
    type: OPERATE_NAMESPACE_SET_KEY_FAILED,
    payload: res,
});

export const OPERATE_NAMESPACE_SET_KEY_IN_PROGRESS = 'OPERATE_NAMESPACE_SET_KEY_IN_PROGRESS';
export type OperateNamespaceSetKeyInProgress = Action<typeof OPERATE_NAMESPACE_SET_KEY_IN_PROGRESS>;

export const operateNamespaceSetKeyInProgress = (): OperateNamespaceSetKeyInProgress => ({
    type: OPERATE_NAMESPACE_SET_KEY_IN_PROGRESS,
});

export type OperateNamespaceSetKeyAction =
    | OperateNamespaceSetKeySuccessful
    | OperateNamespaceSetKeyFailed
    | OperateNamespaceSetKeyInProgress;

export type OperateOperationsAction = OperateNamespaceKeyAction | OperateNamespaceSetKeyAction;