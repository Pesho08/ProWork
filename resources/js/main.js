/**
 * ProWork - Task Management Application with ALERT DEBUGGING
 */

let currentDetailTaskId = null;

window.addEventListener('DOMContentLoaded', () => {
    alert('Page loaded!');
    waitForJavaBridge();
    setupEventListeners();
});

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
}

function attachDeleteButtonListeners() {
  const deleteButtons = document.querySelectorAll('.delete-btn');
  alert('Found ' + deleteButtons.length + ' delete buttons');
  
  deleteButtons.forEach((button, index) => {
    const taskId = button.getAttribute('data-task-id');
    
    button.addEventListener('click', function(e) {
      alert('DELETE BUTTON CLICKED! Task ID: ' + taskId);
      
      e.stopPropagation();
      e.preventDefault();
      
      deleteTask(taskId);
    });
  });
}

function attachTaskClickListeners() {
  const clickableTasks = document.querySelectorAll('.task-item.clickable');
  
  clickableTasks.forEach((taskItem, index) => {
    const taskId = taskItem.getAttribute('data-task-id');
    
    taskItem.addEventListener('click', function(e) {
      if (e.target.classList.contains('delete-btn') || e.target.closest('.delete-btn')) {
        return;
      }
      
      showTaskDetail(taskId);
    });
  });
}

function showTaskForm() {
  document.getElementById('task-form-container').classList.remove('hidden');
  
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('task-deadline').value = today;
}

function hideTaskForm() {
  document.getElementById('task-form-container').classList.add('hidden');
  document.getElementById('task-form').reset();
}

function handleTaskSubmit(e) {
  e.preventDefault();

  const name = document.getElementById('task-name').value;
  const deadline = document.getElementById('task-deadline').value;
  const type = document.getElementById('task-type').value;
  const priority = document.getElementById('task-priority').value;
  const repetition = document.getElementById('task-repetition').value;

  try {
    const taskId = window.javaApp.addTask(name, deadline, type, priority, repetition);
    
    if (taskId) {
      hideTaskForm();
      loadTasks();
    } else {
      alert('Error: Could not add task!');
    }
  } catch (error) {
    alert('Error adding task: ' + error.message);
  }
}

function deleteTask(taskId) {
  alert('deleteTask called with ID: ' + taskId);
  
  if (!taskId) {
    alert('ERROR: No task ID!');
    return;
  }
  
  if (confirm('Are you sure you want to delete this task?')) {
    try {
      alert('Calling Java deleteTask...');
      const success = window.javaApp.deleteTask(taskId);
      alert('Java returned: ' + success);
      
      if (success) {
        alert('Success! Reloading tasks...');
        loadTasks();
      } else {
        alert('Delete returned false!');
      }
    } catch (error) {
      alert('ERROR: ' + error.message);
    }
  }
}

function showTaskDetail(taskId) {
  try {
    const taskJson = window.javaApp.getTask(taskId);
    const task = JSON.parse(taskJson);
    
    document.getElementById('detail-task-name').textContent = task.name;
    document.getElementById('detail-deadline').textContent = task.deadline;
    document.getElementById('detail-priority').textContent = task.priority;
    document.getElementById('detail-notes').value = task.notes || '';
    
    currentDetailTaskId = taskId;
    
    document.getElementById('task-detail-modal').classList.remove('hidden');
  } catch (error) {
    alert('Error loading task details: ' + error.message);
  }
}

function hideTaskDetail() {
  document.getElementById('task-detail-modal').classList.add('hidden');
  currentDetailTaskId = null;
}

function handleSaveNotes() {
  const notes = document.getElementById('detail-notes').value;
  
  try {
    const success = window.javaApp.setTaskNotes(currentDetailTaskId, notes);
    
    if (success) {
      hideTaskDetail();
      loadTasks();
    } else {
      alert('Error: Could not save notes!');
    }
  } catch (error) {
    alert('Error saving notes: ' + error.message);
  }
}

function waitForJavaBridge() {
  if (typeof window.javaApp !== 'undefined') {
    alert('JavaBridge ready!');
    loadTasks();
  } else {
    setTimeout(waitForJavaBridge, 100);
  }
}

function loadTasks() {
  try {
    const tasksJson = window.javaApp.getAllTasks();
    const tasks = JSON.parse(tasksJson);
    displayTasks(tasks);
  } catch (error) {
    alert('Error loading tasks: ' + error.message);
  }
}

function displayTasks(tasks) {
  const taskList = document.getElementById('task-list');

  if (tasks.length === 0) {
    taskList.innerHTML = '<p>No tasks yet. Add your first task!</p>';
    return;
  }

  let html = '<div class="tasks">';

  tasks.forEach(task => {
    const isClickable = task.type === 'TEST';
    const clickableClass = isClickable ? 'clickable' : '';
    
    html += '<div class="task-item ' + clickableClass + '" data-task-id="' + task.id + '" style="border-left: 5px solid ' + task.color + '">';
    html += '  <div class="task-header">';
    html += '    <h3>' + task.name + '</h3>';
    html += '    <div class="task-actions">';
    html += '      <span class="task-type">' + task.type + '</span>';
    html += '      <button class="delete-btn" data-task-id="' + task.id + '">Delete</button>';
    html += '    </div>';
    html += '  </div>';
    html += '  <div class="task-details">';
    html += '    <span><span class="icon">Date:</span> ' + task.deadline + '</span>';
    html += '    <span><span class="icon">Priority:</span> ' + task.priority + '</span>';
    if (task.repetition !== 'NONE') {
      html += '    <span><span class="icon">Repeat:</span> ' + task.repetition + '</span>';
    }
    html += '  </div>';
    if (task.completed) {
      html += '  <span class="task-completed">Completed</span>';
    }
    if (isClickable) {
      html += '  <span class="task-hint">Click to add notes</span>';
    }
    html += '</div>';
  });

  html += '</div>';
  taskList.innerHTML = html;
  
  // ATTACH LISTENERS AFTER RENDERING
  attachDeleteButtonListeners();
  attachTaskClickListeners();
}