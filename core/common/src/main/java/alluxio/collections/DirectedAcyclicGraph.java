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

package alluxio.collections;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * A Directed Acyclic Graph (DAG).
 *
 * @param <T> the payload of each node
 */
@NotThreadSafe
public class DirectedAcyclicGraph<T> {
  private final List<DirectedAcyclicGraphNode<T>> mRoots;
  private final Map<T, DirectedAcyclicGraphNode<T>> mIndex;

  /**
   * A Directed Acyclic Graph (DAG).
   */
  public DirectedAcyclicGraph() {
    mRoots = Lists.newArrayList();
    mIndex = Maps.newHashMap();
  }

  /**
   * Adds a node to the DAG, that takes the given payload and depends on the specified parents.
   *
   * @param payload the payload
   * @param parents the parents of the created node
   */
  public void add(T payload, List<T> parents) {
    // this checks the payload doesn't exist in the graph, and therefore prevents cycles
    Preconditions.checkState(!contains(payload), "the payload already exists in the DAG");

    // construct the new node
    DirectedAcyclicGraphNode<T> newNode = new DirectedAcyclicGraphNode<T>(payload);
    mIndex.put(payload, newNode);

    if (parents.isEmpty()) {
      // add to root
      mRoots.add(newNode);
    } else {
      // find parent nodes
      for (T parent : parents) {
        Preconditions.checkState(contains(parent),
            "the parent payload " + parent + " does not exist in the DAG");
        DirectedAcyclicGraphNode<T> parentNode = mIndex.get(parent);
        parentNode.addChild(newNode);
        newNode.addParent(parentNode);
      }
    }
  }

  /**
   * Deletes a leaf DAG node that carries the given payload.
   *
   * @param payload the payload of the node to delete
   */
  public void deleteLeaf(T payload) {
    Preconditions.checkState(contains(payload), "the node does not exist");
    DirectedAcyclicGraphNode<T> node = mIndex.get(payload);
    Preconditions.checkState(node.getChildren().isEmpty(), "the node is not a leaf");

    // delete from parent
    for (DirectedAcyclicGraphNode<T> parent : node.getParents()) {
      parent.removeChild(node);
    }

    // remove from index
    mIndex.remove(payload);

    if (node.getParents().isEmpty()) {
      mRoots.remove(node);
    }
  }

  /**
   * Checks if a node with the given payload is in the DAG.
   *
   * @param payload the payload of the node to check
   * @return true if there a node in the DAG contains the given value as payload, false otherwise
   */
  public boolean contains(T payload) {
    return mIndex.containsKey(payload);
  }

  /**
   * Gets the payloads for the children of the given node.
   *
   * @param payload the payload of the parent node
   * @return the children's payloads, an empty list if the given payload doesn't exist in the DAG
   */
  public List<T> getChildren(T payload) {
    List<T> children = Lists.newArrayList();
    if (!mIndex.containsKey(payload)) {
      return children;
    }
    DirectedAcyclicGraphNode<T> node = mIndex.get(payload);
    for (DirectedAcyclicGraphNode<T> child : node.getChildren()) {
      children.add(child.getPayload());
    }
    return children;
  }

  /**
   * Gets the payloads for the given nodes parents.
   *
   * @param payload the payload of the children node
   * @return the parents' payloads, an empty list if the given payload doesn't exist in the DAG
   */
  public List<T> getParents(T payload) {
    List<T> parents = Lists.newArrayList();
    if (!mIndex.containsKey(payload)) {
      return parents;
    }
    DirectedAcyclicGraphNode<T> node = mIndex.get(payload);
    for (DirectedAcyclicGraphNode<T> parent : node.getParents()) {
      parents.add(parent.getPayload());
    }
    return parents;
  }

  /**
   * Checks if a given payload is in a root of the DAG.
   *
   * @param payload the payload to check for root
   * @return true if the payload is in the root of the DAG, false otherwise
   */
  public boolean isRoot(T payload) {
    if (!contains(payload)) {
      return false;
    }
    return mRoots.contains(mIndex.get(payload));
  }

  /**
   * Gets the payloads of all the root nodes of the DAG.
   *
   * @return all the root payloads
   */
  public List<T> getRoots() {
    List<T> roots = Lists.newArrayList();
    for (DirectedAcyclicGraphNode<T> root : mRoots) {
      roots.add(root.getPayload());
    }
    return roots;
  }

  /**
   * Sorts a given set of payloads topologically based on the DAG. This method requires all the
   * payloads to be in the DAG.
   *
   * @param payloads the set of input payloads
   * @return the payloads after topogological sort
   */
  public List<T> sortTopologically(Set<T> payloads) {
    List<T> result = Lists.newArrayList();

    Set<T> input = Sets.newHashSet(payloads);
    Deque<DirectedAcyclicGraphNode<T>> toVisit =
        new ArrayDeque<DirectedAcyclicGraphNode<T>>(mRoots);
    while (!toVisit.isEmpty()) {
      DirectedAcyclicGraphNode<T> visit = toVisit.removeFirst();
      T payload = visit.getPayload();
      if (input.remove(payload)) {
        result.add(visit.getPayload());
      }
      toVisit.addAll(visit.getChildren());
    }

    Preconditions.checkState(toVisit.isEmpty(), "Not all the given payloads are in the DAG: ",
        input);
    return result;
  }

  /**
   * Gets all payloads of the DAG in the topological order.
   *
   * @return the payloads of all the nodes in toplogical order
   */
  public List<T> getAllInTopologicalOrder() {
    return sortTopologically(mIndex.keySet());
  }
}
