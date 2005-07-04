/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server;

import marauroa.common.*;
import marauroa.common.game.*;
import marauroa.common.net.*;
import marauroa.server.game.*;

import games.stendhal.common.*;
import games.stendhal.server.entity.*;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import java.util.*;
import java.io.*;
import java.net.*;

import games.stendhal.server.entity.npc.*;
import games.stendhal.server.entity.item.*;
import games.stendhal.server.entity.creature.*;

public class StendhalRPZone extends MarauroaRPZone 
  {
  private List<TransferContent> contents;

  private List<String> entryPoints;
  private List<String> zoneChangePoints;
  private List<Portal> portals;

  private List<NPC> npcs;
  private List<RespawnPoint> respawnPoints;
  private List<Food> foodItems;
  
  private CollisionDetection collisionMap;
  private int width;
  private int height;

  public StendhalRPZone(String name)
    {
    super(name);
    
    contents=new LinkedList<TransferContent>();
    entryPoints=new LinkedList<String>();
    zoneChangePoints=new LinkedList<String>();
    portals=new LinkedList<Portal>();

    npcs=new LinkedList<NPC>();
    respawnPoints=new LinkedList<RespawnPoint>();
    foodItems=new LinkedList<Food>();
    
    collisionMap=new CollisionDetection();
    }
  
  public void onInit() throws Exception
    {
    }
    
  public void onFinish() throws Exception
    {
    }
  
  public List<NPC> getNPCList()
    {
    return npcs;
    }
  
  public List<Portal> getPortals()
    {
    return portals;
    }
  
  public Portal getPortal(int number)
    {
    for(Portal portal: portals)
      {
      if(portal.getNumber()==number)
        {
        return portal;
        }
      }
    
    return null;
    }

  public List<RespawnPoint> getRespawnPointList()
    {
    return respawnPoints;
    }

  public List<Food> getFoodItemList()
    {
    return foodItems;
    }
    
  public void addZoneChange(String entry)
    {
    zoneChangePoints.add(entry);
    }  
  
  public void addPortal(Portal portal)
    {
    portals.add(portal);
    }

  public void addNPC(NPC npc)
    {
    npcs.add(npc);
    }

  public void addEntryPoint(String entry)
    {
    entryPoints.add(0,entry);
    }  
  
  public void placeObjectAtEntryPoint(Entity object)
    {
    if(entryPoints.size()==0)
      {
      return;
      }
      
    String entryPoint=entryPoints.get(0);
    String[] components=entryPoint.split(",");

    object.setx(Integer.parseInt(components[0]));
    object.sety(Integer.parseInt(components[1]));
    }

  public void placeObjectAtZoneChangePoint(StendhalRPZone oldzone, Entity object)
    {
    if(zoneChangePoints.size()==0)
      {
      return;
      }
      
    String exitDirection=null;

    if(object.gety()<4)
      {
      exitDirection="N";
      }
    else if(object.gety()>oldzone.getHeight()-4)
      {
      exitDirection="S";
      }
    else if(object.getx()<4)
      {
      exitDirection="W";
      }
    else if(object.getx()>oldzone.getWidth()-4)
      {
      exitDirection="E";
      }
    else
      {
      // NOTE: If any of the above is true, then it just put object on the first zone change point.
      String[] components=zoneChangePoints.get(0).split(",");
      Logger.trace("StendhalRPZone::placeObjectAtZoneChangePoint","D","Player zone change default: "+components);
      object.setx(Integer.parseInt(components[0]));
      object.sety(Integer.parseInt(components[1]));
      return;
      }
    
    Logger.trace("StendhalRPZone::placeObjectAtZoneChangePoint","D","Player exit direction: "+exitDirection);
    
    int x=0;
    int y=0;
    int distance=Integer.MAX_VALUE;
    String minpoint=zoneChangePoints.get(0);
    
    if(exitDirection.equals("N"))
      {
      x=object.getx();
      y=getHeight();
      }
    else if(exitDirection.equals("S"))
      {
      x=object.getx();
      y=0;
      }
    else if(exitDirection.equals("W"))
      {
      x=getWidth();
      y=object.gety();
      }
    else if(exitDirection.equals("E"))
      {
      x=0;
      y=object.gety();
      }

    Logger.trace("StendhalRPZone::placeObjectAtZoneChangePoint","D","Player entry point: ("+x+","+y+")");

    for(String point: zoneChangePoints)
      {
      String[] components=point.split(",");
      int px=Integer.parseInt(components[0]);
      int py=Integer.parseInt(components[1]);
      
      if((px-x)*(px-x)+(py-y)*(py-y)<distance)
        {
        Logger.trace("StendhalRPZone::placeObjectAtZoneChangePoint","D","Best entry point: ("+px+","+py+") --> "+distance);
        distance=(px-x)*(px-x)+(py-y)*(py-y);
        minpoint=point;
        }
      }
      
    Logger.trace("StendhalRPZone::placeObjectAtZoneChangePoint","D","Choosen entry point: ("+minpoint+") --> "+distance);
    String[] components=minpoint.split(",");
    object.setx(Integer.parseInt(components[0]));
    object.sety(Integer.parseInt(components[1]));
    }
  
  
  public void addLayer(String name, String filename, int version) throws IOException
    {
    Logger.trace("StendhalRPZone::addLayer",">");
    TransferContent content=new TransferContent();
    content.name=name;
    content.cacheable=true;
    content.data=getBytesFromFile(filename);
    content.timestamp=CRC.cmpCRC(content.data);
    
    contents.add(content);
    Logger.trace("StendhalRPZone::addLayer","<");
    }

  public void addCollisionLayer(String name, String filename, int version) throws IOException
    {
    Logger.trace("StendhalRPZone::addCollisionLayer",">");
    TransferContent content=new TransferContent();
    content.name=name;
    content.cacheable=true;
    content.timestamp=version;
    Logger.trace("StendhalRPZone::addLayer","D",Integer.toString(content.timestamp));
    content.data=getBytesFromFile(filename);
    
    contents.add(content);
    
    collisionMap.setCollisionData(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filename))); //new FileReader(filename));
    Logger.trace("StendhalRPZone::addCollisionLayer","<");    
    }
  
  public void populate(String filename) throws IOException, RPObjectInvalidException
    {
    Logger.trace("StendhalRPZone::populate",">");
    
    InputStream in=getClass().getClassLoader().getResourceAsStream(filename);
    BufferedReader file=new BufferedReader(new InputStreamReader(in));
    
    String text=file.readLine();
    String[] size=text.split(" ");
    int width=Integer.parseInt(size[0]);
    int height=Integer.parseInt(size[1]);
    
    int j=0;
    
    while((text=file.readLine())!=null)
      {
      if(text.trim().equals(""))
        {
        break;
        }
        
      String[] items=text.split(",");
      for(String item: items)
        {
        int value=Integer.parseInt(item)-(2401) /* Number of tiles at zelda_outside_chipset */;
        createEntityAt(value,j%width,j/width);
        j++;      
        }
      }

    Logger.trace("StendhalRPZone::populate","<");
    }
  
  protected void createEntityAt(int type, int x, int y)
    {
    try
      {
      switch(type)
        {
        case 1: /* Entry point */
          {
          String entryPoint=new String(x+","+y);
          addEntryPoint(entryPoint);
          break;
          }            
        case 2: /* Zone change  */
          {
          String entryPoint=new String(x+","+y);
          addZoneChange(entryPoint);
          break;
          }
        case 12: /* Rat */
          {
          RespawnPoint point=new RespawnPoint(x,y,2);
          point.set(this, new Rat(),1);
          respawnPoints.add(point);
          
          break;
          }
        case 13: /* Cave rat */
          {
          RespawnPoint point=new RespawnPoint(x,y,2);
          point.set(this, new CaveRat(),1);
          respawnPoints.add(point);
          
          break;
          }
        case 14: /* Wolf */
          {
          RespawnPoint point=new RespawnPoint(x,y,2);
          point.set(this, new Wolf(),1);
          respawnPoints.add(point);
  
          break;
          }
        case 15: /* Cobra */
          {
          RespawnPoint point=new RespawnPoint(x,y,2);
          point.set(this, new Cobra(),1);
          respawnPoints.add(point);
  
          break;
          }
        case 16: /* Orc */
          {
          RespawnPoint point=new RespawnPoint(x,y,2);
          point.set(this, new Orc(),1);
          respawnPoints.add(point);
  
          break;
          }
        case 17: /* Gargoyle */
          {
          RespawnPoint point=new RespawnPoint(x,y,2);
          point.set(this, new Gargoyle(),1);
          respawnPoints.add(point);
  
          break;
          }
        case 18: /* Ogre */
          {
          RespawnPoint point=new RespawnPoint(x,y,2);
          point.set(this, new Ogre(),1);
          respawnPoints.add(point);
  
          break;
          }
        case 19: /* Kobold */
          {
          RespawnPoint point=new RespawnPoint(x,y,2);
          point.set(this, new Kobold(),1);
          respawnPoints.add(point);
  
          break;
          }
        case 20: /* Boar */
          {
          RespawnPoint point=new RespawnPoint(x,y,2);
          point.set(this, new Boar(),1);
          respawnPoints.add(point);
  
          break;
          }
        case 21: /* Troll */
          {
          RespawnPoint point=new RespawnPoint(x,y,2);
          point.set(this, new Troll(),1);
          respawnPoints.add(point);
  
          break;
          }
        case 22: /* Goblin */
          {
          RespawnPoint point=new RespawnPoint(x,y,2);
          point.set(this, new Goblin(),1);
          respawnPoints.add(point);
  
          break;
          }
        case 92: /* Food */
          {
          Food food=new Food();
          assignRPObjectID(food);
          food.setAmount(5);
          food.setx(x);
          food.sety(y);
          add(food);
  
          foodItems.add(food);
          break;
          }
        }
      }
    catch(AttributeNotFoundException e)
      {
      Logger.thrown("StendhalRPZone::populate","X",e);
      }
    }
    
  public int getWidth()
    {
    return collisionMap.getWidth();
    }
  
  public int getHeight()
    {
    return collisionMap.getHeight();
    }
  
  
  public List<TransferContent> getContents()
    {
    return contents;
    }
  
  public boolean leavesZone(Entity entity, double x, double y) throws AttributeNotFoundException  
    {
    Rectangle2D area=entity.getArea(x,y);
    return collisionMap.leavesZone(area);
    }
    
  public boolean simpleCollides(Entity entity, double x, double y) throws AttributeNotFoundException  
    {
    Rectangle2D area=entity.getArea(x,y);
    return collisionMap.collides(area);
    }
    
  public boolean collides(Entity entity, double x, double y) throws AttributeNotFoundException  
    {
    Rectangle2D area=entity.getArea(x,y);
    
    if(collisionMap.collides(area)==false)
      {
      Rectangle2D otherarea=new Rectangle.Double();
      for(RPObject other: objects.values())
        {
        Entity otherEntity=(Entity)other;
        
        if(otherEntity.isCollisionable())
          {
          otherEntity.getArea(otherarea,otherEntity.getx(),otherEntity.gety());
          if(area.intersects(otherarea) && !entity.getID().equals(otherEntity.getID()))
            {
            return true;
            }
          }
        }
              
      return false;
      }   
    else
      {
      return true;
      }    
    }

  private static byte[] getBytesFromFile(String file) throws IOException 
    {
    ByteArrayOutputStream out=new ByteArrayOutputStream();
    InputStream is= StendhalRPZone.class.getClassLoader().getResourceAsStream(file);
    
    if(is==null)
      {
      Logger.trace("StendhalRPZone::init","X", "cannot find file "+file);
      throw new FileNotFoundException(file);
      }
    
    byte[] buffer=new byte[1024];
    int len=0;
    while((len=is.read(buffer,0,1024))!=-1)
      {
      out.write(buffer,0,len);
      }

    return out.toByteArray();
    }
  }
