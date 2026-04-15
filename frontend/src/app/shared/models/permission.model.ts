import { UserDto } from './user.model';

export type PermissionType = 'VIEW' | 'EDIT';

export interface PermissionDto {
  id: number;
  user: UserDto;
  permissionType: PermissionType;
  grantedAt: string;
}

export interface GrantPermissionRequest {
  username: string;
  permissionType: PermissionType;
}

export interface UpdatePermissionRequest {
  permissionType: PermissionType;
}
