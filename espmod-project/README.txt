ESP + Freecam (Fabric, Minecraft 1.21.4)

BUILD THE JAR (no tools needed, uses GitHub):
 1. Make a free account at github.com and create a new repository.
 2. Upload EVERYTHING in this folder (including the hidden .github folder).
    Easiest: unzip, then drag the contents into the repo page ("uploading an existing file").
 3. Open the repo's "Actions" tab -> the "build" run -> download the "espmod-jar" artifact.
 4. Unzip it to get espmod-1.0.0.jar.

BUILD LOCALLY instead: install JDK 21 + Gradle 8.12, then run `gradle build`.
The jar appears in build/libs/espmod-1.0.0.jar (not the -sources one).

INSTALL IN THE MODRINTH APP:
 - Create/open a profile: Fabric loader, Minecraft 1.21.4.
 - Add "Fabric API" from the content browser.
 - Profile -> Add content -> From file -> pick espmod-1.0.0.jar.
   (or Open folder -> mods, and drop the jar in)

CONTROLS:
 Right Shift = menu (toggles ESP, each block type, freecam, speed)
 H = ESP on/off      J = Freecam on/off
 Freecam: WASD + Space/Shift, mouse to look. Auto-exits if you take damage.
