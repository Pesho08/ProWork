/**
 * ProWork - Task Management Application
 * Main JavaScript file handling task display, creation, deletion, and details
 */

// Global variable to store current task ID for detail modal
let currentDetailTaskId = null;

// Wait for DOM and Java Bridge to be ready
window.addEventListener('DOMContentLoaded', () => {
    console.log('Page loaded!');
    waitForJavaBridge();
    setupEventListeners();
});

/**
 * Setup all event listeners for buttons and forms
 */
function setupEventListeners() {
  const addTaskBtn = document.getElementById('add-task-btn');
  const cancelBtn = document.getElementById('cancel-btn');
  const taskForm = document.getElementById('task-form');
  const closeDetailBtn = document.getElementById('close-detail-btn');
  const cancelDetailBtn = document.getElementById('cancel-detail-btn');
  const saveNotesBtn = document.getElementById('save-notes-btn');

  addTaskBtn.addEventListener('click', showTaskForm);
  cancelBtn.addEventListener('click', hideTaskForm);
  taskForm.addEventListener('submit', handleTaskSubmit);
  closeDetailBtn.addEventListener('click', hideTaskDetail);
  cancelDetailBtn.addEventListener('click', hideTaskDetail);
  saveNotesBtn.addEventListener('click', handleSaveNotes);
  
  // Event delegation for delete buttons and task clicks
  document.getElementById('task-list').addEventListener('click', handleTaskListClick);
}

/**
 * Handle clicks on task list (delegation for delete and task clicks)
 * @param {Event} e - Click event
 */
function handleTaskListClick(e) {
  console.log('Click detected on:', e.target);
  
  // Handle delete button click
  if (e.target.classList.contains('delete-btn') || e.target.closest('.delete-btn')) {
    const deleteBtn = e.target.classList.contains('delete-btn') ? e.target : e.target.closest('.delete-btn');
    const taskId = deleteBtn.dataset.taskId;
    console.log('Delete button clicked! Task ID:', taskId);
    deleteTask(taskId);
    return;
  }
  
  // Handle task item click (for TEST type)
  const taskItem = e.target.closest('.task-item.clickable');
  if (taskItem) {
    const taskId = taskItem.dataset.taskId;
    console.log('Task item clicked! Task ID:', taskId);
    showTaskDetail(taskId);
  }
}l

/**
 * Show the task creation form
 */
function showTaskForm() {
  document.getElementById('task-form-container').classList.remove('hidden');
  
  // Set today as default deadline
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('task-deadline').value = today;
}

/**
 * Hide the task creation form and reset it
 */
function hideTaskForm() {
  document.getElementById('task-form-container').classList.add('hidden');
  document.getElementById('task-form').reset();
}

/**
 * Handle task form submission
 * @param {Event} e - Form submit event
 */
function handleTaskSubmit(e) {
  e.preventDefault(); // Prevent page reload

  // Get form data
  const name = document.getElementById('task-name').value;
  const deadline = document.getElementById('task-deadline').value;
  const type = document.getElementById('task-type').value;
  const priority = document.getElementById('task-priority').value;
  const repetition = document.getElementById('task-repetition').value;

  console.log('Adding task:', { name, deadline, type, priority, repetition });

  try {
    // Call Java backend to add task
    const taskId = window.javaApp.addTask(name, deadline, type, priority, repetition);
    
    if (taskId) {
      console.log('Task added successfully! ID:', taskId);
      
      // Close form and reload task list
      hideTaskForm();
      loadTasks();
    } else {
      alert('Error: Could not add task!');
    }
  } catch (error) {
    console.error('Error adding task:', error);
    alert('Error adding task: ' + error.message);
  }
}

/**
 * Delete a task by ID
 * @param {string} taskId - The ID of the task to delete
 */
function deleteTask(taskId) {
  if (confirm('Are you sure you want to delete this task?')) {
    try {
      const success = window.javaApp.deleteTask(taskId);
      
      if (success) {
        console.log('Task deleted successfully!');
        loadTasks(); // Reload task list
      } else {
        alert('Error: Could not delete task!');
      }
    } catch (error) {
      console.error('Error deleting task:', error);
      alert('Error deleting task: ' + error.message);
    }
  }
}

/**
 * Show task detail modal (only for TEST type tasks)
 * @param {string} taskId - The ID of the task to show
 */
function showTaskDetail(taskId) {
  try {
    // Get task details from Java backend
    const taskJson = window.javaApp.getTask(taskId);
    const task = JSON.parse(taskJson);
    
    // Populate modal with task data
    document.getElementById('detail-task-name').textContent = task.name;
    document.getElementById('detail-deadline').textContent = task.deadline;
    document.getElementById('detail-priority').textContent = task.priority;
    document.getElementById('detail-notes').value = task.notes || '';
    
    // Store task ID for saving notes later
    currentDetailTaskId = taskId;
    
    // Show modal
    document.getElementById('task-detail-modal').classList.remove('hidden');
  } catch (error) {
    console.error('Error showing task detail:', error);
    alert('Error loading task details: ' + error.message);
  }
}

/**
 * Hide the task detail modal
 */
function hideTaskDetail() {
  document.getElementById('task-detail-modal').classList.add('hidden');
  currentDetailTaskId = null;
}

/**
 * Save notes for a TEST task
 */
function handleSaveNotes() {
  const notes = document.getElementById('detail-notes').value;
  
  try {
    const success = window.javaApp.setTaskNotes(currentDetailTaskId, notes);
    
    if (success) {
      console.log('Notes saved successfully!');
      hideTaskDetail();
      loadTasks(); // Reload to show any updates
    } else {
      alert('Error: Could not save notes!');
    }
  } catch (error) {
    console.error('Error saving notes:', error);
    alert('Error saving notes: ' + error.message);
  }
}

/**
 * Wait for Java Bridge to be available
 * Polls every 100ms until window.javaApp is defined
 */
function waitForJavaBridge() {
  if (typeof window.javaApp !== 'undefined') {
    console.log('JavaBridge ready!');
    loadTasks();
  } else {
    console.log('Waiting for JavaBridge...');
    setTimeout(waitForJavaBridge, 100);
  }
}

/**
 * Load all tasks from Java backend and display them
 */
function loadTasks() {
  try {
    // Get tasks as JSON string from Java backend
    const tasksJson = window.javaApp.getAllTasks();
    console.log('Tasks JSON:', tasksJson);

    // Parse JSON string to JavaScript array
    const tasks = JSON.parse(tasksJson);
    console.log('Parsed Tasks:', tasks);

    // Display tasks in UI
    displayTasks(tasks);
  } catch (error) {
    console.error('Error loading tasks:', error);
    document.getElementById('task-list').innerHTML = '<p style="color: red;">Error loading tasks: ' + error.message + '</p>';
  }
}

/**
 * Display tasks in the UI
 * @param {Array} tasks - Array of task objects
 */
function displayTasks(tasks) {
  const taskList = document.getElementById('task-list');

  // Show message if no tasks exist
  if (tasks.length === 0) {
    taskList.innerHTML = '<p>No tasks yet. Add your first task!</p>';
    return;
  }

  // Build HTML for all tasks
  let html = '<div class="tasks">';

  tasks.forEach(task => {
    // Determine if task is clickable (only TEST type)
    const isClickable = task.type === 'TEST';
    const clickableClass = isClickable ? 'clickable' : '';
    
    html += `
      <div class="task-item ${clickableClass}" style="border-left: 5px solid ${task.color}" data-task-id="${task.id}">
        <div class="task-header">
          <h3>${task.name}</h3>
          <div class="task-actions">
            <span class="task-type">${task.type}</span>
            <button class="delete-btn" data-task-id="${task.id}" title="Delete task">
              🗑️
            </button>
          </div>
        </div>
        <div class="task-details">
          <span class="task-deadline">📅 ${task.deadline}</span>
          <span class="task-priority">⚡ ${task.priority}</span>
          ${task.repetition !== 'NONE' ? `<span class="task-repetition">🔁 ${task.repetition}</span>` : ''}
        </div>
        ${task.completed ? '<span class="task-completed">✅ Completed</span>' : ''}
        ${isClickable ? '<span class="task-hint">💡 Click to add notes</span>' : ''}
      </div>
    `;
  });

  html += '</div>';
  taskList.innerHTML = html;
}