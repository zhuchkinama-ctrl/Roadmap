import { UserDto } from './user.model';

export type TrackRole = 'OWNER' | 'VIEW' | 'EDIT';

export interface TrackDto {
  id: number;
  title: string;
  description?: string;
  owner: UserDto;
  myRole: TrackRole;
  createdAt: string;
  updatedAt: string;
}

export interface TrackSummaryDto {
  id: number;
  title: string;
  description?: string;
  owner: UserDto;
  myRole: TrackRole;
  updatedAt: string;
}

export interface CreateTrackRequest {
  title: string;
  description?: string;
}

export interface UpdateTrackRequest {
  title: string;
  description?: string;
}

export interface TrackPageResponse {
  content: TrackSummaryDto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
