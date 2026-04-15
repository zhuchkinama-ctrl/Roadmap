import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService } from '../../core/services/admin.service';
import { UserAdminDto } from '../../shared/models';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-panel.component.html',
  styleUrls: ['./admin-panel.component.scss']
})
export class AdminPanelComponent implements OnInit {
  private adminService = inject(AdminService);
  private router = inject(Router);

  users: UserAdminDto[] = [];
  loading = false;
  errorMessage = '';

  // Пагинация
  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;

  // Фильтрация
  searchQuery = '';

  // Изменение роли
  showRoleDialog = false;
  roleUser: UserAdminDto | null = null;
  roleForm = {
    role: 'USER' as 'USER' | 'ADMIN'
  };
  roleLoading = false;
  roleError = '';

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';

    this.adminService.getUsers(this.page, this.size).subscribe({
      next: (response) => {
        this.users = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Ошибка загрузки пользователей';
        this.loading = false;
      }
    });
  }

  onPageChange(newPage: number): void {
    this.page = newPage;
    this.loadUsers();
  }

  onSizeChange(newSize: number): void {
    this.size = newSize;
    this.page = 0;
    this.loadUsers();
  }

  toggleUserLock(user: UserAdminDto): void {
    const action = user.enabled ? 'заблокировать' : 'разблокировать';
    if (!confirm(`Вы уверены, что хотите ${action} пользователя ${user.username}?`)) {
      return;
    }

    if (user.enabled) {
      this.adminService.lockUser(user.id).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (error) => {
          alert(error.error?.message || 'Ошибка блокировки пользователя');
        }
      });
    } else {
      this.adminService.unlockUser(user.id).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (error) => {
          alert(error.error?.message || 'Ошибка разблокировки пользователя');
        }
      });
    }
  }

  deleteUser(user: UserAdminDto): void {
    if (!confirm(`Вы уверены, что хотите удалить пользователя ${user.username}? Все его треки и заметки будут удалены.`)) {
      return;
    }

    this.adminService.deleteUser(user.id).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (error) => {
        alert(error.error?.message || 'Ошибка удаления пользователя');
      }
    });
  }

  openRoleDialog(user: UserAdminDto): void {
    this.roleUser = user;
    this.roleForm = { role: user.role };
    this.roleError = '';
    this.showRoleDialog = true;
  }

  closeRoleDialog(): void {
    this.showRoleDialog = false;
    this.roleUser = null;
    this.roleForm = { role: 'USER' };
    this.roleError = '';
  }

  changeUserRole(): void {
    if (!this.roleUser) return;

    this.roleLoading = true;
    this.roleError = '';

    this.adminService.changeUserRole(this.roleUser.id, this.roleForm.role).subscribe({
      next: () => {
        this.roleLoading = false;
        this.closeRoleDialog();
        this.loadUsers();
      },
      error: (error) => {
        this.roleError = error.error?.message || 'Ошибка изменения роли';
        this.roleLoading = false;
      }
    });
  }

  getRoleBadgeClass(role: string): string {
    return role === 'ADMIN' ? 'badge-admin' : 'badge-user';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
