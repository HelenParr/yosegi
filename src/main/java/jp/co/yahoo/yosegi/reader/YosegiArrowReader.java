/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.yahoo.yosegi.reader;

import jp.co.yahoo.yosegi.config.Configuration;
import jp.co.yahoo.yosegi.message.design.StructContainerField;
import jp.co.yahoo.yosegi.spread.expression.IExpressionNode;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.ValueVector;

import java.io.IOException;

public class YosegiArrowReader {

  private final IArrowLoader arrowLoader;

  /**
   * Initialize without schema definition.
   */
  public YosegiArrowReader(
      final YosegiReader reader , final Configuration config ) throws IOException {
    IRootMemoryAllocator rootAllocator = new DynamicSchemaRootMemoryAllocator();
    BufferAllocator allocator = new RootAllocator( Integer.MAX_VALUE );
    if ( config.containsKey( "spread.reader.expand.column" )
        || config.containsKey( "spread.reader.flatten.column" ) ) {
      arrowLoader = new DynamicArrowLoader( rootAllocator , reader , allocator );
    } else {
      arrowLoader = new DirectArrowLoader( rootAllocator , reader , allocator );
    }
  }

  /**
   * Perform schema definition and initialize.
   */
  public YosegiArrowReader(
      final StructContainerField schema ,
      final YosegiReader reader ,
      final Configuration config ) throws IOException {
    IRootMemoryAllocator rootAllocator = new FixedSchemaRootMemoryAllocator( schema );
    BufferAllocator allocator = new RootAllocator( Integer.MAX_VALUE );
    if ( config.containsKey( "spread.reader.expand.column" )
        || config.containsKey( "spread.reader.flatten.column" ) ) {
      arrowLoader = new DynamicArrowLoader( rootAllocator , reader , allocator );
    } else {
      arrowLoader = new DirectArrowLoader( rootAllocator , reader , allocator );
    }
  }

  public void setNode( final IExpressionNode node ) {
    arrowLoader.setNode( node );
  }

  public boolean hasNext() throws IOException {
    return arrowLoader.hasNext();
  }

  public ValueVector next() throws IOException {
    return arrowLoader.next();
  }

  public void close() throws IOException {
    arrowLoader.close();
  }

}
