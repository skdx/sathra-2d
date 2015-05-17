# Tutorial 0x0: Hello World! #

## Start Sathra ##

To get started, make your `Activity` class extends `SathraActivity`.

```
public class Activity extends SathraActivity {

  @Override
  protected Parameters getParameters() {
     ...
  }

  @Override
  protected void onEngineInitiated() {
     ...
  }

  @Override
  protected abstract void onUpdate(long time, long timeDelta) {
     ...
  }

}
```

The first method we have to override is `getParameters()`. It returns an instance of `Parameters` class which contains various engine properties, such as desired resolution, screen orientation and others. Head to the API reference for more info. The parameters are pretty much self explanatory.

```
@Override
protected Parameters getParameters() {
   return new Parameters();
}
```

You don't have to modify any of the properties as they are all to default values initially. For instance, if you leave `Parameters.width` or `Parameters.height` set to `0`, Sathra will automatically set resolution to drawing surface's dimension.

## Data Driven ##

Right now, if you run our program, it will display black screen. To see something more, we need to load a scene. But first, we have to create it. Big advantage of Sathra 2D is that it's data driven. What does it mean?

From Wikipedia:

> _In computer programming, data-driven programming is a programming  paradigm in which the program statements describe the data to be matched  and the processing required rather than defining a sequence of steps to  be taken._

This approach separates the data from the actual code. There are many advantages to this. You designer is able to create maps without the need of recompiling the application. You can use or create third party tools that will aid you in the process of creating the content. See how it look in practice.

## Create a scene ##

A scene is a [graph](http://en.wikipedia.org/wiki/Scene_graph). A data structure contained of nodes. Each node can have indefinite number of children. A transformation (position, rotation and scale) of a child is always relative to it's parent. Simply put, scene is a way of arranging things on a screen. There are many types of nodes. Right now will use a TextNode. It simply displays a given text with a selected font and has a nice feature of color tags.

Create a new json file in the Android's asset directory. Let's call it `sathra.json`. Paste the following text to it:

```
{
  "class": "eu.sathra.scene.TextNode",
  "font": {
    "font_path": "visitor1.ttf",
    "size": 30
  },
  "text": "Hello world!"
}
```

If you look close enough, it doesn't look very complex. What we need is a font. Get this awesome [Visitor Font](http://www.dafont.com/visitor.font) or any other you like, and place the ttf file in the assets dir, next to our json file. Our scene is almost ready.

## Load a scene ##

To load a scene call `loadScene` from inside `onEngineInitiated ` method.

```
@Override
protected void onEngineInitiated() {
  SceneNode myNode = loadScene("sathra.json"); // path to our json file
  addNode(myNode); // ad it to the root node
}
```

There's only one thing that this scene lacks now, and it's a Camera! We can add it in json, but we'll do it from code this time. So the whole method should look like this:

```
@Override
protected void onEngineInitiated() {
  SceneNode myNode = loadScene("sathra.json"); // path to our json file
  addNode(myNode); // ad it to the root node
  SceneNode camera = CameraNode(this, null, new Transform(), true, null, null, null);
  addNode(camera);
}
```

## Add some colour ##

Try to change the line:

`"text": "Hello world!"`

to:

`"text": "^4Hello ^3world!"`.