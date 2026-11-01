let allTodos = [];
let currentFilter = 'all';
let searchQuery = '';

const motivationalQuotes = [
  "You're doing amazing, sweetie! 💖",
  "One lovely step at a time! 🌸",
  "Small daily victories make big dreams come true! ✨",
  "Keep blooming beautifully! 🌷",
  "You've got this, superstar! 🎀",
  "Sparkle and make things happen! 🧁"
];

document.addEventListener('DOMContentLoaded', () => {
  displayCurrentDate();
  fetchTodos();
});

function displayCurrentDate() {
  const options = { weekday: 'short', month: 'short', day: 'numeric' };
  const today = new Date().toLocaleDateString('en-US', options);
  document.getElementById('currentDateBadge').textContent = `📅 ${today} 🌸`;
}

async function fetchTodos() {
  try {
    const res = await fetch('api/todos');
    if (!res.ok) throw new Error('Failed to fetch todos');
    const data = await res.json();
    allTodos = data.todos || [];
    renderTodos();
  } catch (err) {
    renderTodos();
  }
}

async function handleCreateTodo(e) {
  e.preventDefault();
  const titleInput = document.getElementById('todoTitle');
  const categorySelect = document.getElementById('todoCategory');
  const prioritySelect = document.getElementById('todoPriority');
  const dueDateInput = document.getElementById('todoDueDate');

  const title = titleInput.value.trim();
  if (!title) return;

  const payload = {
    title: title,
    category: categorySelect.value,
    priority: prioritySelect.value,
    dueDate: dueDateInput.value.trim() || 'Today 🌸',
    completed: false
  };

  try {
    const res = await fetch('api/todos', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const result = await res.json();
    if (result.success && result.todo) {
      allTodos.unshift(result.todo);
      titleInput.value = '';
      renderTodos();
    }
  } catch (err) {
    const fallback = {
      id: Date.now(),
      title: payload.title,
      category: payload.category,
      priority: payload.priority,
      dueDate: payload.dueDate,
      completed: false
    };
    allTodos.unshift(fallback);
    titleInput.value = '';
    renderTodos();
  }
}

async function toggleTodo(id, currentStatus) {
  const newStatus = !currentStatus;
  const item = allTodos.find(t => t.id === id);
  if (item) item.completed = newStatus;
  renderTodos();

  try {
    await fetch('api/todos', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id: id, completed: newStatus })
    });
  } catch (err) {}
}

async function deleteTodo(id) {
  const index = allTodos.findIndex(t => t.id === id);
  if (index === -1) return;

  allTodos.splice(index, 1);
  renderTodos();

  try {
    await fetch(`api/todos?id=${id}`, {
      method: 'DELETE'
    });
  } catch (err) {}
}

function setFilter(filter, element) {
  currentFilter = filter;
  document.querySelectorAll('.filter-chips .chip').forEach(c => c.classList.remove('active'));
  if (element) element.classList.add('active');
  renderTodos();
}

function handleSearch(query) {
  searchQuery = query.toLowerCase().trim();
  renderTodos();
}

function renderTodos() {
  const container = document.getElementById('todosContainer');

  const totalCount = allTodos.length;
  const completedCount = allTodos.filter(t => t.completed).length;
  const activeCount = totalCount - completedCount;
  const highCount = allTodos.filter(t => t.priority === 'HIGH').length;

  document.getElementById('countAll').textContent = totalCount;
  document.getElementById('countActive').textContent = activeCount;
  document.getElementById('countCompleted').textContent = completedCount;
  document.getElementById('countHigh').textContent = highCount;

  const pct = totalCount === 0 ? 0 : Math.round((completedCount / totalCount) * 100);
  document.getElementById('progressBarFill').style.width = `${pct}%`;
  document.getElementById('progressSummary').textContent = `Tasks: ${completedCount} / ${totalCount} completed 🌷 (${pct}%)`;

  const quoteEl = document.getElementById('progressQuote');
  if (pct === 100 && totalCount > 0) {
    quoteEl.textContent = "🎀 All done! You're a true queen! 👑✨";
  } else {
    quoteEl.textContent = motivationalQuotes[Math.floor(Math.random() * motivationalQuotes.length)];
  }

  let filtered = allTodos.filter(todo => {
    if (searchQuery && !todo.title.toLowerCase().includes(searchQuery)) {
      return false;
    }
    if (currentFilter === 'active') return !todo.completed;
    if (currentFilter === 'completed') return todo.completed;
    if (currentFilter === 'high') return todo.priority === 'HIGH';
    return true;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <span class="empty-icon">☁️🌸</span>
        <h3 class="empty-title">No tasks found here!</h3>
        <p class="empty-subtitle">${searchQuery ? "Try searching for something else ✨" : "Relax and enjoy a sweet moment or add a new task above 💖"}</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(todo => `
    <div class="todo-item ${todo.completed ? 'completed' : ''}" id="todo-${todo.id}">
      <div class="todo-left">
        <div class="custom-checkbox" onclick="toggleTodo(${todo.id}, ${todo.completed})">
          ${todo.completed ? '✓' : ''}
        </div>
        <div class="todo-content">
          <div class="todo-title">${escapeHtml(todo.title)}</div>
          <div class="todo-meta">
            <span class="category-tag">${escapeHtml(todo.category || 'General 🌸')}</span>
            <span class="priority-tag priority-${todo.priority || 'MEDIUM'}">${todo.priority || 'MEDIUM'}</span>
            <span class="due-tag">📅 ${escapeHtml(todo.dueDate || 'Today')}</span>
          </div>
        </div>
      </div>
      <div class="todo-right">
        <button class="btn-delete" title="Delete Task" onclick="deleteTodo(${todo.id})">
          ✕
        </button>
      </div>
    </div>
  `).join('');
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/[&<>"']/g, m => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[m]));
}
