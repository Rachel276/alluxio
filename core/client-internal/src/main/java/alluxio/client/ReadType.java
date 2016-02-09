/*
 * Licensed to the University of California, Berkeley under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package alluxio.client;

import alluxio.annotation.PublicApi;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Convenience modes for commonly used read types.
 *
 * For finer grained control over data storage, advanced users may specify
 * {@link AlluxioStorageType} and {@link UnderStorageType}.
 */
@PublicApi
@ThreadSafe
public enum ReadType {
  /**
   * Read the file and skip Alluxio storage. This read type will not cause any data migration or
   * eviction in Alluxio storage.
   */
  NO_CACHE(1),
  /**
   * Read the file and cache it in the highest tier of a local worker. This read type will not move
   * data between tiers of Alluxio Storage. Users should use {@link #CACHE_PROMOTE} for more
   * optimized performance with tiered storage.
   */
  CACHE(2),
  /**
   * Read the file and cache it in a local worker. Additionally, if the file was in Alluxio
   * storage, it will be promoted to the top storage layer.
   */
  CACHE_PROMOTE(3);

  private final int mValue;

  ReadType(int value) {
    mValue = value;
  }

  /**
   * @return the {@link AlluxioStorageType} associated with this read type
   */
  public AlluxioStorageType getAlluxioStorageType() {
    if (isPromote()) { // CACHE_PROMOTE
      return AlluxioStorageType.PROMOTE;
    }
    if (isCache()) { // CACHE
      return AlluxioStorageType.STORE;
    }
    // NO_CACHE
    return AlluxioStorageType.NO_STORE;
  }

  /**
   * @return the read type value
   */
  public int getValue() {
    return mValue;
  }

  /**
   * @return true if the read type is {@link #CACHE}, false otherwise
   */
  public boolean isCache() {
    return mValue == CACHE.mValue || mValue == CACHE_PROMOTE.mValue;
  }

  /**
   * @return true if the read type is {@link #CACHE_PROMOTE}, false otherwise
   */
  public boolean isPromote() {
    return mValue == CACHE_PROMOTE.mValue;
  }
}
