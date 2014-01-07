/*
 * The MIT License
 *
 * Copyright 2013 ineaugh.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package piranha.Map.Places;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import piranha.Map.Map;
import piranha.Map.Movable;

/**
 *
 * @author ineaugh
 */
public class Tower
{
  PlaceBase floors[];
  Dimension baseSize, topSize;
  List<Passage> floorPassages[];
  Random rand;
  
  public Tower(Dimension baseSize, Dimension topSize, Random rand, PlaceBase... floors)
  {
    this.floors = floors;
    this.baseSize = new Dimension(baseSize);
    this.topSize = new Dimension(topSize);
    this.floorPassages = new List[floors.length];
    this.rand = rand;
  }
  
  static class TowerPassage extends TwoWayPassage
  {
    Tower tower;
    int destinationFloor;
    
    public TowerPassage(Tower tower, int destinationFloor, char symbol, String actionName, Map destination, Point destinationPoint, Map departure, Point departurePoint)
    {
      super(symbol, actionName, destination, destinationPoint, departure, departurePoint);
      this.tower = tower;
      this.destinationFloor = destinationFloor;
    }

    @Override
    public void Activated(Movable byWhom)
    {
      tower.GetFloor(destinationFloor); // ensure the floor is generated
      super.Activated(byWhom);
    }    
  }
  
  public PlaceBase GetFloor(int floor)
  {
    if(!floors[floor].IsInitialized())
    {
      int lastFloor = floors.length - 1;
      Dimension size = new Dimension((baseSize.width * (lastFloor - floor) + topSize.width * floor) / lastFloor,
        (baseSize.height * (lastFloor - floor) + topSize.height * floor) / lastFloor);
      
      List<Point> specialPoints = new ArrayList<>();
      Map prevFloor = floor > 0 ? floors[floor - 1] : null;
      Map nextFloor = floor < lastFloor ? floors[floor + 1] : null;
      Point prevFloorPoint = null, nextFloorPoint = null;
      for(Passage p : floorPassages[floor])
      {
        specialPoints.add(p.destinationPoint);
        if(p instanceof TowerPassage)
        {
          TowerPassage tp = (TowerPassage)p;
          if(tp.departure == prevFloor)
            prevFloorPoint = tp.departurePoint;
          
          if(tp.departure == nextFloor)
            nextFloorPoint = tp.departurePoint;
        }
      }
      
      floors[floor].Initialize(size, rand, specialPoints);
    }
    
    return floors[floor];
  }
}
