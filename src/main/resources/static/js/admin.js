// ---------------- CSRF ----------------
// const csrfToken = document.querySelector('meta[name="_csrf"]').content;
// const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

// ---------------- DOM ----------------
const usersTable = document.getElementById('usersTable');
const editModal = document.getElementById('editModal');
const deleteModal = document.getElementById('deleteModal');
const userInfo = document.getElementById('userInfo');

// ---------------- Пользователь ----------------
async function loadCurrentUser() {
    try {
        const resp = await fetch('/api/user/current');
        const user = await resp.json();
        document.getElementById('currentUsername').innerText = user.username;
        userInfo.innerHTML = `
            <p><strong>ID:</strong> ${user.id}</p>
            <p><strong>Username:</strong> ${user.username}</p>
            <p><strong>Roles:</strong> ${user.roles.map(r => r.name).join(', ')}</p>
        `;
    } catch(e) {
        console.error('Ошибка загрузки текущего пользователя', e);
    }
}

// ---------------- Загрузка всех пользователей ----------------
async function loadUsers() {
    try {
        const resp = await fetch('/api/admin/users');
        const users = await resp.json();
        usersTable.innerHTML = '';
        users.forEach(u => {
            usersTable.innerHTML += `
                <tr>
                    <td>${u.id}</td>
                    <td>${u.username}</td>
                    <td>${u.roles.map(r=>r.name).join(', ')}</td>
                    <td><button class="btn btn-info btn-sm" onclick="openEdit(${u.id})">Редактировать</button></td>
                    <td><button class="btn btn-danger btn-sm" onclick="openDelete(${u.id})">Удалить</button></td>
                </tr>
            `;
        });
    } catch(e) {
        console.error('Ошибка загрузки пользователей', e);
    }
}

// ---------------- Модальные окна ----------------
async function openEdit(id) {
    const resp = await fetch(`/api/admin/users/${id}`);
    const u = await resp.json();

    editModal.innerHTML = `
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
            <h5 class="modal-title">Редактировать пользователя</h5>
            <button class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
            <input type="text" id="editId" class="form-control mb-2" value="${u.id}" readonly>
            <input type="text" id="editUsername" class="form-control mb-2" value="${u.username}">
            <input type="password" id="editPassword" class="form-control mb-2" placeholder="оставить пустым">
            <select id="editRoles" multiple class="form-select">
                <option value="1" ${u.roles.some(r=>r.name==="ROLE_USER")?'selected':''}>ROLE_USER</option>
                <option value="3" ${u.roles.some(r=>r.name==="ROLE_ADMIN")?'selected':''}>ROLE_ADMIN</option>
            </select>
        </div>
        <div class="modal-footer">
            <button class="btn btn-secondary" data-bs-dismiss="modal">Отмена</button>
            <button class="btn btn-primary" onclick="save(${u.id})">Сохранить</button>
        </div>
      </div>
    </div>`;

    new bootstrap.Modal(editModal).show();
}

async function openDelete(id) {
    deleteModal.innerHTML = `
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
            <h5 class="modal-title">Удалить пользователя</h5>
            <button class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
            <input type="text" class="form-control" value="ID: ${id}" readonly>
        </div>
        <div class="modal-footer">
            <button class="btn btn-secondary" data-bs-dismiss="modal">Отмена</button>
            <button class="btn btn-danger" onclick="removeUser(${id})">Удалить</button>
        </div>
      </div>
    </div>`;

    new bootstrap.Modal(deleteModal).show();
}

// ---------------- CRUD ----------------
async function save(id) {
    const username = document.getElementById('editUsername').value;
    const password = document.getElementById('editPassword').value;
    const roles = [...document.getElementById('editRoles').selectedOptions].map(o => parseInt(o.value));

    const body = {
        username,
        password,
        roleIds: roles
    };

    await fetch(`/api/admin/users/${id}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    });

    bootstrap.Modal.getInstance(editModal).hide();
    loadUsers(); // обновляем таблицу
}

async function removeUser(id) {
    await fetch(`/api/admin/users/${id}`, {
        method: 'DELETE',
        headers: {
        }
    });
    bootstrap.Modal.getInstance(deleteModal).hide();
    loadUsers();
}

document.getElementById('createUserForm').addEventListener('submit', async e => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const roles = [...formData.getAll('roles')].map(r => parseInt(r));

    const params = new URLSearchParams();
    roles.forEach(r => params.append('roleIds', r));

    const body = {
        username: formData.get('username'),
        password: formData.get('password'),
        roleIds: roles
    };

    await fetch(`/api/admin/users`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(body)
    });

    e.target.reset();
    loadUsers();
    loadCurrentUser();
});

// ---------------- Таб переключение ----------------
userTab.addEventListener('click', () => {
    adminPanel.style.display = 'none'; // скрываем админ-панель
    userPanel.style.display = 'block'; // показываем пользовательскую панель

    adminTab.classList.remove('active'); // убираем активность с админ-вкладки
    userTab.classList.add('active');     // делаем активной пользовательскую вкладку

    loadCurrentUser(); // подгружаем данные текущего пользователя
});

adminTab.addEventListener('click', () => {
    userPanel.style.display = 'none';   // скрываем пользовательскую панель
    adminPanel.style.display = 'block'; // показываем админ-панель

    userTab.classList.remove('active'); // убираем активность с userTab
    adminTab.classList.add('active');   // делаем активной adminTab

    loadUsers(); // подгружаем список пользователей
});


// ---------------- Первая загрузка ----------------
document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
    loadCurrentUser();
});
