/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.client.file.options;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import alluxio.CommonTestUtils;
import alluxio.thrift.MountTOptions;

import org.junit.Test;

/**
 * Tests for the {@link MountOptions} class.
 */
public final class MountOptionsTest {
  /**
   * Tests creating a default {@link MountOptions} instance.
   */
  @Test
  public void defaults() {
    MountOptions options = MountOptions.defaults();
    assertFalse(options.isReadOnly());
    assertFalse(options.isShared());
  }

  @Test
  public void readOnlyField() {
    assertTrue(MountOptions.defaults().setReadOnly(true).isReadOnly());
    assertFalse(MountOptions.defaults().setReadOnly(false).isReadOnly());
  }

  @Test
  public void sharedField() {
    assertTrue(MountOptions.defaults().setShared(true).isShared());
    assertFalse(MountOptions.defaults().setShared(false).isShared());
  }

  /**
   * Tests conversion to thrift representation.
   */
  @Test
  public void toThrift() {
    MountOptions options = MountOptions.defaults();
    MountTOptions thriftOptions = options.toThrift();
    assertFalse(thriftOptions.isReadOnly());

    options.setReadOnly(true);
    options.setShared(true);
    thriftOptions = options.toThrift();
    assertTrue(thriftOptions.isReadOnly());
    assertTrue(thriftOptions.isShared());
  }

  @Test
  public void equalsTest() throws Exception {
    CommonTestUtils.testEquals(MountOptions.class);
  }
}
