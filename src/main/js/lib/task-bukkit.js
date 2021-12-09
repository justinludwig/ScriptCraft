'use strict';
/*global __plugin, module, server*/
function bukkitSetTimeout(callback, delayInMillis) {
  var delay = Math.ceil(delayInMillis / 50);
  // https://github.com/walterhiggins/ScriptCraft/issues/396
  var Run = Java.type('java.lang.Runnable');
  var MyRun = Java.extend(Run, { run: callback });
  var task = server.scheduler.runTaskLater(__plugin, new MyRun(), delay);
  return task;
}
function bukkitClearTimeout(task) {
  task.cancel();
}
function bukkitSetInterval(callback, intervalInMillis) {
  var delay = Math.ceil(intervalInMillis / 50);
  // https://github.com/walterhiggins/ScriptCraft/issues/396
  var Run = Java.type('java.lang.Runnable');
  var MyRun = Java.extend(Run, { run: callback });
  var task = server.scheduler.runTaskTimer(__plugin, new MyRun(), delay, delay);
  return task;
}
function bukkitClearInterval(bukkitTask) {
  bukkitTask.cancel();
}
module.exports = function($) {
  $.setTimeout = bukkitSetTimeout;
  $.clearTimeout = bukkitClearTimeout;
  $.setInterval = bukkitSetInterval;
  $.clearInterval = bukkitClearInterval;
};
