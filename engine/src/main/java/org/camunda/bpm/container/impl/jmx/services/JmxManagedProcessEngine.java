/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jmx.services;

import org.camunda.bpm.container.impl.jmx.kernel.MBeanService;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;

/**
 * <p>Represents a process engine managed by the {@link MBeanServiceContainer}</p>
 * 
 * @author Daniel Meyer
 *
 */
public class JmxManagedProcessEngine implements MBeanService<ProcessEngine>, JmxManagedProcessEngineMBean {
  
  protected ProcessEngineConfiguration processEngineConfiguration;
  protected ProcessEngine processEngine;
  
  // for subclasses
  protected JmxManagedProcessEngine() {
  }
  
  public JmxManagedProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }
  
  public void start(MBeanServiceContainer contanier) {
    // this one has no lifecycle support
  }
  
  public void stop(MBeanServiceContainer container) {
    // this one has no lifecycle support
  }
  
  public String getName() {
    return processEngine.getName();
  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }
  
  public ProcessEngine getValue() {
    return processEngine;
  }

}
