/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.14.505 on 2019-09-06 16:31:39.

export interface BucketDTO {
    name: string;
    description: string;
    bucketType: string;
}

export interface BucketTypeDTO {
    name: string;
    description: string;
}

export interface FolderDTO {
    parentUniqueId: string;
    name: string;
}

export interface MembershipDTO {
    email: string;
    permissionCreate: boolean;
    permissionDelete: boolean;
}

export interface ResourceDTO {
    type: TYPE;
    leaf: boolean;
    uniqueKey: string;
    name: string;
    childs: ResourceDTO[];
    content: any;
}

export interface TeamDTO {
    uuid: string;
    name: string;
    description: string;
    members: UserRoleDTO[];
    buckets: BucketDTO[];
}

export interface UserRoleDTO {
    email: string;
    longName: string;
    admin: boolean;
    active: boolean;
}

export type TYPE = "BUCKET" | "FOLDER" | "CONTENT";
