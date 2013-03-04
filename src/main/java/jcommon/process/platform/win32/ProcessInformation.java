package jcommon.process.platform.win32;

import com.sun.jna.Pointer;
import jcommon.process.IEnvironmentVariable;
import jcommon.process.IProcess;
import jcommon.process.IProcessListener;
import jcommon.process.api.win32.Win32;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static jcommon.process.api.win32.Win32.*;

final class ProcessInformation implements IProcess {
  public static final int
      MAX_SEQUENCE_NUMBER = 5001
  ;

  final int pid;
  final HANDLE process;
  final HANDLE main_thread;
  final HANDLE stdout_child_process_read;
  final HANDLE stderr_child_process_read;
  final HANDLE stdin_child_process_write;

  final AtomicBoolean starting = new AtomicBoolean(true);
  final AtomicBoolean closing = new AtomicBoolean(false);

  final String[] command_line;
  final IProcessListener[] listeners;
  final boolean inherit_parent_environment;
  final IEnvironmentVariable[] environment_variables;
  final CountDownLatch exit_latch = new CountDownLatch(1);
  final AtomicInteger exit_value = new AtomicInteger(0);

  final IWriteCallback write_callback;

  public static interface IWriteCallback {
    boolean write(ByteBuffer bb);
  }

  public ProcessInformation(final int pid, final HANDLE process, final HANDLE main_thread, final HANDLE stdout_child_process_read, final HANDLE stderr_child_process_read, final HANDLE stdin_child_process_write, final boolean inherit_parent_environment, final IEnvironmentVariable[] environment_variables, final String[] command_line, final IProcessListener[] listeners, final IWriteCallback write_callback) {
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

    this.write_callback = write_callback;
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

  @Override
  public boolean write(byte b[]) {
    return write(b, 0, b.length);
  }

  @Override
  public boolean write(byte b[], int off, int len) {
    return write(ByteBuffer.wrap(b, off, len));
  }

  @Override
  public boolean write(ByteBuffer bb) {
    return write_callback.write(bb);
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
      //t.printStackTrace();
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
