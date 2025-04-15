# Image Mod

A simple hacked together mod for creation of pixel art in Minecraft

### Automatically Generate Pixel-Art With A Single Command. Any Image File Is Turned Into Minecraft Blocks In Less Than A Second!

**The command:** _/image_
![Create image command "/image create"](https://cdn.modrinth.com/data/cached_images/c0e8f666f7a00f7131775be5530eade6828abfa1.jpeg)

## Usage
You are given a few dimension options for creating your pixel-art:

_/image create <ImagePath> -width <width>_ - **create image with a defined block width (height to scale)**

_/image create <ImagePath> -height <height>_ - **create image with a defined block height (width to scale)**

_/image create <ImagePath> <width> <height>_ - **create image with a defined block width & height**

![Create command select image "/image create '... options.jpg'"](https://cdn.modrinth.com/data/cached_images/b930d8199e6e8ee1905d5cd4780d7afeedc021fe.jpeg)
![Create command dimensions selection "/image create 'mcr.jpg' ...-width/-height/~ ~"](https://cdn.modrinth.com/data/cached_images/3f3a69c86810fbd6fb2b8c6789a55c9dacc3152f.jpeg)
![Image command width selected, full command "/image create 'mcr.jpg' -width 200"](https://cdn.modrinth.com/data/cached_images/ed60edfc37111532c55eb26fdc0dfef032face1d.jpeg)
![Create image, successfully created 200x112 image, popped into view](https://cdn.modrinth.com/data/cached_images/e589445afff30e887f2ea408233ef3ae5f2f2273.jpeg)
![Beautiful view of the full image made up entirely just out of existing minecraft blocks](https://cdn.modrinth.com/data/cached_images/45f1821989f8fef612eeb18424f554e9ddcfbdab.jpeg)

## Configuration
![Configure with setDirectory command "/image setDirectory 'path on your computer'"](https://cdn.modrinth.com/data/cached_images/e91eb5773bf42205ff1673692bfaa40ff10bef35.jpeg)

## Future
- Make setDirectory persistent\
- Fix falling blocks on surface\
- Multiplayer/Server/Plugin support\
- Fix update on resource pack reload\
- Improve block filter further\