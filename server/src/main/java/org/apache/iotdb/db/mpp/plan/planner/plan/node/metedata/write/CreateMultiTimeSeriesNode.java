/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iotdb.db.mpp.plan.planner.plan.node.metedata.write;

import org.apache.iotdb.common.rpc.thrift.TRegionReplicaSet;
import org.apache.iotdb.commons.exception.IllegalPathException;
import org.apache.iotdb.commons.path.PartialPath;
import org.apache.iotdb.db.mpp.plan.analyze.Analysis;
import org.apache.iotdb.db.mpp.plan.planner.plan.node.PlanNode;
import org.apache.iotdb.db.mpp.plan.planner.plan.node.PlanNodeId;
import org.apache.iotdb.db.mpp.plan.planner.plan.node.PlanNodeType;
import org.apache.iotdb.db.mpp.plan.planner.plan.node.PlanVisitor;
import org.apache.iotdb.db.mpp.plan.planner.plan.node.WritePlanNode;
import org.apache.iotdb.tsfile.exception.NotImplementedException;
import org.apache.iotdb.tsfile.file.metadata.enums.CompressionType;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.file.metadata.enums.TSEncoding;
import org.apache.iotdb.tsfile.utils.ReadWriteIOUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CreateMultiTimeSeriesNode extends WritePlanNode {
  private List<PartialPath> paths = new ArrayList<>();
  private List<TSDataType> dataTypes = new ArrayList<>();
  private List<TSEncoding> encodings = new ArrayList<>();
  private List<CompressionType> compressors = new ArrayList<>();
  private List<String> aliasList;
  private List<Map<String, String>> propsList;
  private List<Map<String, String>> tagsList;
  private List<Map<String, String>> attributesList;
  private List<Long> tagOffsets;
  private TRegionReplicaSet regionReplicaSet;

  public CreateMultiTimeSeriesNode(PlanNodeId id) {
    super(id);
  }

  public CreateMultiTimeSeriesNode(
      PlanNodeId id,
      List<PartialPath> paths,
      List<TSDataType> dataTypes,
      List<TSEncoding> encodings,
      List<CompressionType> compressors,
      List<Map<String, String>> propsList,
      List<String> aliasList,
      List<Map<String, String>> tagsList,
      List<Map<String, String>> attributesList) {
    super(id);
    this.paths = paths;
    this.dataTypes = dataTypes;
    this.encodings = encodings;
    this.compressors = compressors;
    this.propsList = propsList;
    this.aliasList = aliasList;
    this.tagsList = tagsList;
    this.attributesList = attributesList;
  }

  public List<PartialPath> getPaths() {
    return paths;
  }

  public void setPaths(List<PartialPath> paths) {
    this.paths = paths;
  }

  public List<TSDataType> getDataTypes() {
    return dataTypes;
  }

  public void setDataTypes(List<TSDataType> dataTypes) {
    this.dataTypes = dataTypes;
  }

  public List<TSEncoding> getEncodings() {
    return encodings;
  }

  public void setEncodings(List<TSEncoding> encodings) {
    this.encodings = encodings;
  }

  public List<CompressionType> getCompressors() {
    return compressors;
  }

  public void setCompressors(List<CompressionType> compressors) {
    this.compressors = compressors;
  }

  public List<Map<String, String>> getPropsList() {
    return propsList;
  }

  public void setPropsList(List<Map<String, String>> propsList) {
    this.propsList = propsList;
  }

  public List<String> getAliasList() {
    return aliasList;
  }

  public void setAliasList(List<String> aliasList) {
    this.aliasList = aliasList;
  }

  public List<Map<String, String>> getTagsList() {
    return tagsList;
  }

  public void setTagsList(List<Map<String, String>> tagsList) {
    this.tagsList = tagsList;
  }

  public List<Map<String, String>> getAttributesList() {
    return attributesList;
  }

  public void setAttributesList(List<Map<String, String>> attributesList) {
    this.attributesList = attributesList;
  }

  public List<Long> getTagOffsets() {
    return tagOffsets;
  }

  public void setTagOffsets(List<Long> tagOffsets) {
    this.tagOffsets = tagOffsets;
  }

  public void addTimeSeries(
      PartialPath path,
      TSDataType dataType,
      TSEncoding encoding,
      CompressionType compressor,
      Map<String, String> props,
      String alias,
      Map<String, String> tags,
      Map<String, String> attributes) {
    this.paths.add(path);
    this.dataTypes.add(dataType);
    this.encodings.add(encoding);
    this.compressors.add(compressor);
    if (props != null) {
      if (this.propsList == null) {
        propsList = new ArrayList<>();
      }
      propsList.add(props);
    }
    if (alias != null) {
      if (this.aliasList == null) {
        aliasList = new ArrayList<>();
      }
      aliasList.add(alias);
    }
    if (tags != null) {
      if (this.tagsList == null) {
        tagsList = new ArrayList<>();
      }
      tagsList.add(tags);
    }
    if (attributes != null) {
      if (this.attributesList == null) {
        attributesList = new ArrayList<>();
      }
      attributesList.add(attributes);
    }
  }

  @Override
  public List<PlanNode> getChildren() {
    return new ArrayList<>();
  }

  @Override
  public void addChild(PlanNode child) {}

  @Override
  public PlanNode clone() {
    throw new NotImplementedException("Clone of CreateMultiTimeSeriesNode is not implemented");
  }

  @Override
  public int allowedChildCount() {
    return NO_CHILD_ALLOWED;
  }

  @Override
  public List<String> getOutputColumnNames() {
    return null;
  }

  @Override
  public <R, C> R accept(PlanVisitor<R, C> visitor, C schemaRegion) {
    return visitor.visitCreateMultiTimeSeries(this, schemaRegion);
  }

  public static CreateMultiTimeSeriesNode deserialize(ByteBuffer byteBuffer) {
    String id;
    List<PartialPath> paths;
    List<TSDataType> dataTypes;
    List<TSEncoding> encodings;
    List<CompressionType> compressors;
    List<String> aliasList = null;
    List<Map<String, String>> propsList = null;
    List<Map<String, String>> tagsList = null;
    List<Map<String, String>> attributesList = null;

    int size = byteBuffer.getInt();
    paths = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      try {
        paths.add(new PartialPath(ReadWriteIOUtils.readString(byteBuffer)));
      } catch (IllegalPathException e) {
        throw new IllegalArgumentException("Can not deserialize CreateMultiTimeSeriesNode", e);
      }
    }

    dataTypes = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      dataTypes.add(TSDataType.values()[byteBuffer.get()]);
    }

    encodings = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      encodings.add(TSEncoding.values()[byteBuffer.get()]);
    }

    compressors = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      compressors.add(CompressionType.values()[byteBuffer.get()]);
    }

    byte label = byteBuffer.get();
    if (label >= 0) {
      aliasList = new ArrayList<>();
      if (label == 1) {
        for (int i = 0; i < size; i++) {
          aliasList.add(ReadWriteIOUtils.readString(byteBuffer));
        }
      }
    }

    label = byteBuffer.get();
    if (label >= 0) {
      propsList = new ArrayList<>();
      if (label == 1) {
        for (int i = 0; i < size; i++) {
          propsList.add(ReadWriteIOUtils.readMap(byteBuffer));
        }
      }
    }

    label = byteBuffer.get();
    if (label >= 0) {
      tagsList = new ArrayList<>();
      if (label == 1) {
        for (int i = 0; i < size; i++) {
          tagsList.add(ReadWriteIOUtils.readMap(byteBuffer));
        }
      }
    }

    label = byteBuffer.get();
    if (label >= 0) {
      attributesList = new ArrayList<>();
      if (label == 1) {
        for (int i = 0; i < size; i++) {
          attributesList.add(ReadWriteIOUtils.readMap(byteBuffer));
        }
      }
    }

    id = ReadWriteIOUtils.readString(byteBuffer);

    return new CreateMultiTimeSeriesNode(
        new PlanNodeId(id),
        paths,
        dataTypes,
        encodings,
        compressors,
        propsList,
        aliasList,
        tagsList,
        attributesList);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateMultiTimeSeriesNode that = (CreateMultiTimeSeriesNode) o;
    return this.getPlanNodeId().equals(that.getPlanNodeId())
        && Objects.equals(paths, that.paths)
        && Objects.equals(dataTypes, that.dataTypes)
        && Objects.equals(encodings, that.encodings)
        && Objects.equals(compressors, that.compressors)
        && Objects.equals(propsList, that.propsList)
        && Objects.equals(tagOffsets, that.tagOffsets)
        && Objects.equals(aliasList, that.aliasList)
        && Objects.equals(tagsList, that.tagsList)
        && Objects.equals(attributesList, that.attributesList);
  }

  @Override
  protected void serializeAttributes(ByteBuffer byteBuffer) {
    PlanNodeType.CREATE_MULTI_TIME_SERIES.serialize(byteBuffer);

    // paths
    byteBuffer.putInt(paths.size());
    for (PartialPath path : paths) {
      ReadWriteIOUtils.write(path.getFullPath(), byteBuffer);
    }

    // dataTypes
    for (TSDataType dataType : dataTypes) {
      byteBuffer.put((byte) dataType.ordinal());
    }

    // encodings
    for (TSEncoding encoding : encodings) {
      byteBuffer.put((byte) encoding.ordinal());
    }

    // compressors
    for (CompressionType compressor : compressors) {
      byteBuffer.put((byte) compressor.ordinal());
    }

    // alias
    if (aliasList == null) {
      byteBuffer.put((byte) -1);
    } else if (aliasList.isEmpty()) {
      byteBuffer.put((byte) 0);
    } else {
      byteBuffer.put((byte) 1);
      for (String alias : aliasList) {
        ReadWriteIOUtils.write(alias, byteBuffer);
      }
    }

    // props
    if (propsList == null) {
      byteBuffer.put((byte) -1);
    } else if (propsList.isEmpty()) {
      byteBuffer.put((byte) 0);
    } else {
      byteBuffer.put((byte) 1);
      for (Map<String, String> props : propsList) {
        ReadWriteIOUtils.write(props, byteBuffer);
      }
    }

    // tags
    if (tagsList == null) {
      byteBuffer.put((byte) -1);
    } else if (tagsList.isEmpty()) {
      byteBuffer.put((byte) 0);
    } else {
      byteBuffer.put((byte) 1);
      for (Map<String, String> tags : tagsList) {
        ReadWriteIOUtils.write(tags, byteBuffer);
      }
    }

    // attributes
    if (attributesList == null) {
      byteBuffer.put((byte) -1);
    } else if (attributesList.isEmpty()) {
      byteBuffer.put((byte) 0);
    } else {
      byteBuffer.put((byte) 1);
      for (Map<String, String> attributes : attributesList) {
        ReadWriteIOUtils.write(attributes, byteBuffer);
      }
    }
  }

  public int hashCode() {
    return Objects.hash(
        this.getPlanNodeId(),
        paths,
        dataTypes,
        encodings,
        compressors,
        tagOffsets,
        aliasList,
        tagsList,
        attributesList);
  }

  @Override
  public TRegionReplicaSet getRegionReplicaSet() {
    return regionReplicaSet;
  }

  public void setRegionReplicaSet(TRegionReplicaSet regionReplicaSet) {
    this.regionReplicaSet = regionReplicaSet;
  }

  @Override
  public List<WritePlanNode> splitByPartition(Analysis analysis) {
    Map<TRegionReplicaSet, CreateMultiTimeSeriesNode> splitMap = new HashMap<>();
    for (int i = 0; i < paths.size(); i++) {
      TRegionReplicaSet regionReplicaSet =
          analysis.getSchemaPartitionInfo().getSchemaRegionReplicaSet(paths.get(i).getDevice());
      CreateMultiTimeSeriesNode tmpNode;
      if (splitMap.containsKey(regionReplicaSet)) {
        tmpNode = splitMap.get(regionReplicaSet);
      } else {
        tmpNode = new CreateMultiTimeSeriesNode(this.getPlanNodeId());
        tmpNode.setRegionReplicaSet(regionReplicaSet);
        splitMap.put(regionReplicaSet, tmpNode);
      }
      tmpNode.addTimeSeries(
          paths.get(i),
          dataTypes.get(i),
          encodings.get(i),
          compressors.get(i),
          propsList == null ? null : propsList.get(i),
          aliasList == null ? null : aliasList.get(i),
          attributesList == null ? null : tagsList.get(i),
          attributesList == null ? null : attributesList.get(i));
    }
    return new ArrayList<>(splitMap.values());
  }
}