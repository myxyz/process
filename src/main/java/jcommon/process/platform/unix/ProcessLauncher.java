/*
  Copyright (C) 2013 the original author or authors.

  See the LICENSE.txt file distributed with this work for additional
  information regarding copyright ownership.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package jcommon.process.platform.unix;

import jcommon.core.platform.IPlatformImplementation;
import jcommon.core.platform.unix.UnixPlatformProvider;
import jcommon.process.IEnvironmentVariable;
import jcommon.process.IEnvironmentVariableBlock;
import jcommon.process.IProcess;
import jcommon.process.IProcessListener;
import jcommon.process.platform.IProcessLauncher;

/**
 */
public final class ProcessLauncher extends UnixPlatformProvider implements IProcessLauncher {
  public static final IProcessLauncher INSTANCE = new ProcessLauncher();

  @Override
  public IPlatformImplementation provideImplementation() {
    return INSTANCE;
  }

  @Override
  public IEnvironmentVariableBlock requestParentEnvironmentVariableBlock() {
    return null;
  }

  @Override
  public IProcess launch(final boolean inherit_parent_environment, final IEnvironmentVariableBlock environment_variables, final String[] command_line, final IProcessListener[] listeners) {
    return IProcessLauncher.DEFAULT.launch(inherit_parent_environment, environment_variables, command_line, listeners);
  }
}
