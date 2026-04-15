export interface NoteDto {
  id: number;
  trackId: number;
  title: string;
  content?: string;
  parentId?: number;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
}

export interface NoteTreeNodeDto {
  id: number;
  title: string;
  content?: string;
  orderIndex: number;
  children: NoteTreeNodeDto[];
}

export interface CreateNoteRequest {
  title: string;
  content?: string;
  parentId?: number;
}

export interface UpdateNoteRequest {
  title: string;
  content?: string;
}

export interface MoveNoteRequest {
  parentId?: number;
  orderIndex?: number;
}
