/*
 * WARNING: DO NOT EDIT THIS FILE. This file is generated by yarn gen. Any changes will be overwritten.
 */

import { RestClientError, RestClientRole, User } from '../api';
import SimpleResponse from '../types/SimpleResponse';

export interface GetRolesStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * SimpleResponse
     */
    readonly successValue: SimpleResponse;
    readonly inProgress: boolean;
}

export interface CreateRoleStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * SimpleResponse
     */
    readonly successValue: SimpleResponse;
    readonly inProgress: boolean;
}

export interface GetRoleStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * RestClientRole
     */
    readonly successValue: RestClientRole;
    readonly inProgress: boolean;
}

export interface DropRoleStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * SimpleResponse
     */
    readonly successValue: SimpleResponse;
    readonly inProgress: boolean;
}

export interface GrantPrivilegesStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * SimpleResponse
     */
    readonly successValue: SimpleResponse;
    readonly inProgress: boolean;
}

export interface RevokePrivilegesStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * SimpleResponse
     */
    readonly successValue: SimpleResponse;
    readonly inProgress: boolean;
}

export interface GetUsersStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * SimpleResponse
     */
    readonly successValue: SimpleResponse;
    readonly inProgress: boolean;
}

export interface CreateUserStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * SimpleResponse
     */
    readonly successValue: SimpleResponse;
    readonly inProgress: boolean;
}

export interface GetUserStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * User
     */
    readonly successValue: User;
    readonly inProgress: boolean;
}

export interface DropUserStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * SimpleResponse
     */
    readonly successValue: SimpleResponse;
    readonly inProgress: boolean;
}

export interface ChangePasswordStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * SimpleResponse
     */
    readonly successValue: SimpleResponse;
    readonly inProgress: boolean;
}

export interface GrantRolesStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * SimpleResponse
     */
    readonly successValue: SimpleResponse;
    readonly inProgress: boolean;
}

export interface RevokeRolesStateFields {
    /**
     * RestClientError
     */
    readonly errorValue: RestClientError;
    /**
     * SimpleResponse
     */
    readonly successValue: SimpleResponse;
    readonly inProgress: boolean;
}

export interface AdminOperationsStateFields {
    readonly getRoles: GetRolesStateFields;
    readonly createRole: CreateRoleStateFields;
    readonly getRole: GetRoleStateFields;
    readonly dropRole: DropRoleStateFields;
    readonly grantPrivileges: GrantPrivilegesStateFields;
    readonly revokePrivileges: RevokePrivilegesStateFields;
    readonly getUsers: GetUsersStateFields;
    readonly createUser: CreateUserStateFields;
    readonly getUser: GetUserStateFields;
    readonly dropUser: DropUserStateFields;
    readonly changePassword: ChangePasswordStateFields;
    readonly grantRoles: GrantRolesStateFields;
    readonly revokeRoles: RevokeRolesStateFields;
}

export type AdminOperationsState = AdminOperationsStateFields;