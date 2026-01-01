/**
 * ProWork - Task Management Application
 * Main JavaScript file
 */

let currentDetailTaskId = null;

window.addEventListener('DOMContentLoaded', function() {
    console.log('ProWork loaded');
    waitForJavaBridge();
    setupEventListeners();
});

function setupEventListeners() {
  document.getElementById('add-task-btn').addEventListener('click', showTaskForm);
  document.getElementById('cancel-btn').addEventListener('click', hideTaskForm);
  document.getElementById('task-form').addEventListener('submit', handleTaskSubmit);
  document.getElementById('close-detail-btn').addEventListener('click', hideTaskDetail);
  document.getElementById('cancel-detail-btn').addEventListener('click', hideTaskDetail);
  document.getElementById('save-notes-btn').addEventListener('click', handleSaveNotes);
}

function attachDeleteButtonListeners() {
  const deleteButtons = document.querySelectorAll('.delete-btn');
  
  deleteButtons.forEach(function(button) {
    const taskId = button.getAttribute('data-task-id');
    
    button.addEventListener('click', function(e) {
      e.stopPropagation();
      e.preventDefault();
      deleteTask(taskId);
    });
  });
}

function attachTaskClickListeners() {
  const clickableTasks = document.querySelectorAll('.task-item.clickable');
  
  clickableTasks.forEach(function(taskItem) {
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
    console.error('Error adding task:', error);
    alert('Error adding task: ' + error.message);
  }
}

function deleteTask(taskId) {
  if (!taskId) {
    alert('Error: No task ID!');
    return;
  }
  
  if (confirm('Are you sure you want to delete this task?')) {
    try {
      const success = window.javaApp.deleteTask(taskId);
      
      if (success) {
        loadTasks();
      } else {
        alert('Error: Could not delete task!');
      }
    } catch (error) {
      console.error('Error deleting task:', error);
      alert('Error deleting task: ' + error.message);
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
    console.error('Error showing task detail:', error);
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
    console.error('Error saving notes:', error);
    alert('Error saving notes: ' + error.message);
  }
}

function waitForJavaBridge() {
  if (typeof window.javaApp !== 'undefined') {
    console.log('JavaBridge ready');
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
    console.error('Error loading tasks:', error);
    document.getElementById('task-list').innerHTML = '<p style="color:red;">Error loading tasks</p>';
  }
}

function displayTasks(tasks) {
  const taskList = document.getElementById('task-list');

  if (tasks.length === 0) {
    taskList.innerHTML = '<p>No tasks yet. Add your first task!</p>';
    return;
  }

  let html = '<div class="tasks">';

  tasks.forEach(function(task) {
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
  
  attachDeleteButtonListeners();
  attachTaskClickListeners();
}