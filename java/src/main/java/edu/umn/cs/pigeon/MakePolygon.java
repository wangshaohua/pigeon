/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the
 * NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package edu.umn.cs.pigeon;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.Tuple;

import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Segment;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.ogc.OGCPolygon;

/**
 * Generates a geometry of type Polygon out of a bag of points. The last point
 * in the bag should be the same as the first point.
 * @author Ahmed Eldawy
 */
public class MakePolygon extends EvalFunc<DataByteArray>{
  
  private GeometryParser geometryParser = new GeometryParser();

  @Override
  public DataByteArray exec(Tuple b) throws IOException {
    DataBag points = (DataBag) b.get(0);
    Point[] coordinates = new Point[(int) points.size()];
    int i = 0;
    for (Tuple t : points) {
      coordinates[i++] =
          (Point) (geometryParser.parseGeom(t.get(0))).getEsriGeometry();
    }
    Polygon multi_path = new Polygon();
    for (i = 1; i <coordinates.length; i++) {
      Segment segment = new Line();
      segment.setStart(coordinates[i-1]);
      segment.setEnd(coordinates[i]);
      multi_path.addSegment(segment, false);
    }
    OGCPolygon linestring = new OGCPolygon(multi_path, 0,
        SpatialReference.create(4326));
    return new DataByteArray(linestring.asBinary().array());
  }

}
