package jcommon.process.platform.win32;

import jcommon.process.IEnvironmentVariable;
import jcommon.process.IProcess;
import jcommon.process.IProcessListener;
import jcommon.process.api.win32.Win32;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

final class ProcessInformation implements IProcess {
  final int pid;
  final Win32.HANDLE process;
  final Win32.HANDLE main_thread;
  final Win32.HANDLE process_exit_wait;
  final Win32.HANDLE stdout_child_process_read;
  final Win32.HANDLE stderr_child_process_read;
  final Win32.HANDLE stdin_child_process_write;

  final AtomicBoolean starting = new AtomicBoolean(true);
  final AtomicBoolean closing = new AtomicBoolean(false);

  final String[] command_line;
  final IProcessListener[] listeners;
  final boolean inherit_parent_environment;
  final IEnvironmentVariable[] environment_variables;
  final CountDownLatch exit_latch = new CountDownLatch(1);
  final AtomicInteger exit_value = new AtomicInteger(0);

  public static interface ICreateProcessExitCallback {
    Win32.HANDLE createExitCallback(ProcessInformation process_info);
  }

  public ProcessInformation(final int pid, final Win32.HANDLE process, final Win32.HANDLE main_thread, final Win32.HANDLE stdout_child_process_read, final Win32.HANDLE stderr_child_process_read, final Win32.HANDLE stdin_child_process_write, final boolean inherit_parent_environment, final IEnvironmentVariable[] environment_variables, final String[] command_line, final IProcessListener[] listeners, final ICreateProcessExitCallback callback) {
    this.pid = pid;
    this.process = process;
    this.main_thread = main_thread;
    this.stdout_child_process_read = stdout_child_process_read;
    this.stderr_child_process_read = stderr_child_process_read;
    this.stdin_child_process_write = stdin_child_process_write;

    this.command_line = command_line;
    this.listeners = listeners;
    this.inherit_parent_environment = inherit_parent_environment;
    this.environment_variables = environment_variables;

    this.process_exit_wait = callback != null ? callback.createExitCallback(this) : null;
  }

  /**
   * @see IProcess#isParentEnvironmentInherited()
   */
  @Override
  public boolean isParentEnvironmentInherited() {
    return inherit_parent_environment;
  }

  /**
   * @see IProcess#getPID()
   */
  @Override
  public int getPID() {
    return pid;
  }

  /**
   * @see IProcess#getCommandLine()
   */
  @Override
  public String[] getCommandLine() {
    return command_line;
  }

  /**
   * @see IProcess#getEnvironmentVariables()
   */
  @Override
  public IEnvironmentVariable[] getEnvironmentVariables() {
    return environment_variables;
  }

  /**
   * @see IProcess#getListeners()
   */
  @Override
  public IProcessListener[] getListeners() {
    return listeners;
  }

  @Override
  public int getExitCode() {
    return exit_value.get();
  }

  @Override
  public boolean await() {
    try {
      exit_latch.await();
      return true;
    } catch(InterruptedException ignored) {
      return false;
    } catch(Throwable t) {
      return false;
    }
  }

  @Override
  public boolean await(long timeout, TimeUnit unit) {
    try {
      return exit_latch.await(timeout, unit);
    } catch(InterruptedException ignored) {
      return false;
    } catch(Throwable t) {
      return false;
    }
  }

  @Override
  public boolean waitFor() {
    return await();
  }

  @Override
  public boolean waitFor(long timeout, TimeUnit unit) {
    return await(timeout, unit);
  }

  public void notifyStarted() {
    try {
      for(IProcessListener listener : listeners) {
        listener.started(this);
      }
    } catch(Throwable t) {
      notifyError(t);
    }
  }

  public void notifyStopped(final int exit_code) {
    try {
      for(IProcessListener listener : listeners) {
        listener.stopped(this, exit_code);
      }
    } catch(Throwable t) {
      notifyError(t);
    }
  }

  public void notifyStdOut(final ByteBuffer buffer, final int bufferSize) {
    try {
      for(IProcessListener listener : listeners) {
        listener.stdout(this, buffer, bufferSize);
      }
    } catch(Throwable t) {
      notifyError(t);
    }
  }

  public void notifyStdErr(final ByteBuffer buffer, final int bufferSize) {
    try {
      for(IProcessListener listener : listeners) {
        listener.stderr(this, buffer, bufferSize);
      }
    } catch(Throwable t) {
      notifyError(t);
    }
  }

  public void notifyError(final Throwable t) {
    for(IProcessListener listener : listeners) {
      //try {
        listener.error(this, t);
      //} catch(Throwable ignored) {
      //}
    }
  }
}