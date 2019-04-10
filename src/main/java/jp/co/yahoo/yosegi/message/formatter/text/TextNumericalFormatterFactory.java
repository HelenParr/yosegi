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

package jp.co.yahoo.yosegi.message.formatter.text;

import jp.co.yahoo.yosegi.message.objects.PrimitiveObject;
import jp.co.yahoo.yosegi.message.parser.IParser;
import jp.co.yahoo.yosegi.util.ByteArrayData;
import jp.co.yahoo.yosegi.util.CascadingDispatcherFactory;

import java.io.IOException;
import java.util.Objects;


class TextNumericalFormatterFactory {
  @FunctionalInterface
  public interface WriteFunc {
    public void accept(ByteArrayData buffer, Object obj) throws IOException;
  }

  @FunctionalInterface
  public interface WriteParserFunc {
    void accept(ByteArrayData buffer, PrimitiveObject obj, IParser parser) throws IOException;
  }

  @FunctionalInterface
  public interface StringNumber {
    public String apply(Number obj) throws IOException;
  }

  @FunctionalInterface
  public interface StringPrimitiveObject {
    public String apply(PrimitiveObject obj) throws IOException;
  }

  @FunctionalInterface
  private interface WriteDispatcherFunc {
    public String apply(Object obj) throws IOException;
  }

  private final StringNumber stringNumber;
  private final StringPrimitiveObject stringPrimitiveObject;

  public TextNumericalFormatterFactory(
      final StringNumber stringNumber,
      final StringPrimitiveObject stringPrimitiveObject) {
    this.stringNumber = stringNumber;
    this.stringPrimitiveObject = stringPrimitiveObject;
  }

  private static void write(final ByteArrayData buffer, String str) throws IOException {
    buffer.append(str.getBytes("UTF-8"));
  }

  public WriteFunc createWriteFunc() {
    CascadingDispatcherFactory<WriteDispatcherFunc> sw = new CascadingDispatcherFactory<>();
    sw.set(Number.class, obj -> stringNumber.apply((Number)obj));
    sw.set(PrimitiveObject.class, obj -> stringPrimitiveObject.apply((PrimitiveObject)obj));
    CascadingDispatcherFactory.Func<WriteDispatcherFunc> writeDispatcher = sw.create();

    return (buffer, obj) -> {
      WriteDispatcherFunc func = writeDispatcher.get(obj);
      if (Objects.nonNull(func)) {
        write(buffer, func.apply(obj));
      }
    };
  }

  public WriteParserFunc createWriteParserFunc() {
    return (buffer, obj, parser) -> {
      write(buffer, stringPrimitiveObject.apply((PrimitiveObject)obj));
    };
  }
}
