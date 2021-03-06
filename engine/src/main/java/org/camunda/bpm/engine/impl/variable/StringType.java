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
package org.camunda.bpm.engine.impl.variable;



/**
 * @author Tom Baeyens
 */
public class StringType implements VariableType {

  public static final String TYPE_NAME = "string";

  public String getTypeName() {
    return TYPE_NAME;
  }

  public boolean isCachable() {
    return true;
  }

  public Object getValue(ValueFields valueFields) {
    return valueFields.getTextValue();
  }

  public void setValue(Object value, ValueFields valueFields) {
    valueFields.setTextValue((String) value);
  }

  public boolean isAbleToStore(Object value) {
    if (value==null) {
      return true;
    }
    return String.class.isAssignableFrom(value.getClass());
  }
  
  public String getTypeNameForValue(Object value) {
    return value.getClass().getSimpleName();
  }
}
