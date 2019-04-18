package net.hypixel.resourcepack.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.hypixel.resourcepack.Converter;
import net.hypixel.resourcepack.PackConverter;
import net.hypixel.resourcepack.Util;
import net.hypixel.resourcepack.pack.Pack;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import net.hypixel.resourcepack.impl.NameConverter.Mapping;

public class ModelConverter extends Converter {

    public ModelConverter(PackConverter packConverter) {
        super(packConverter);
    }

    @Override
    public void convert(Pack pack) throws IOException {
        Path models = pack.getWorkingPath().resolve("assets" + File.separator + "minecraft" + File.separator + "models");

        remapModelJson(models.resolve("block"));
        remapModelJson(models.resolve("item"));
        remapModelJson(models.resolve("itemblock"));
    }

    protected void remapModelJson(Path path) throws IOException {
        if (!path.toFile().exists()) return;

        NameConverter nameConverter = packConverter.getConverter(NameConverter.class);
        Files.list(path)
                //.filter(path1 -> path1.toString().endsWith(".json"))
                .forEach(model -> {
                    try {
                        if(model.toString().endsWith(".json")) {
                            System.out.println(model.toString());
                            JsonObject jsonObject = Util.readJson(packConverter.getGson(), model);

                            // minify the json so we can replace spaces in paths easily
                            // TODO Improvement: handle this in a cleaner way?
                            String content = jsonObject.toString();
                            content = content.replaceAll("items/", "item/");
                            content = content.replaceAll("blocks/", "block/");
                            content = content.replaceAll(" ", "_");
                            content = content.toLowerCase();
                            Files.write(model, Collections.singleton(content), Charset.forName("UTF-8"));

                            // handle the remapping of textures, for models that use default texture names
                            jsonObject = Util.readJson(packConverter.getGson(), model);
                            if (jsonObject.has("textures")) {

                                JsonObject textureObject = jsonObject.getAsJsonObject("textures");
                                for (Map.Entry<String, JsonElement> entry : textureObject.entrySet()) {
                                    String value = entry.getValue().getAsString();
                                    remap(value,entry.getKey(),textureObject,nameConverter);
                                }
                            }
                            if (jsonObject.has("parent")) {
                                //System.out.println("Remapping parent");
                                String value = jsonObject.get("parent").getAsString();
                                //System.out.println("Value of parent: " + value);
                                //parentObject.getAsString();
                                //String value = parentObject.getAsString();
                                remap(value,"parent",jsonObject,nameConverter);
                                /*if (value.startsWith("block/")) {
                                    jsonObject.addProperty("parent", "block/" + nameConverter.getBlockMapping().remap(value.substring("block/".length())));
                                } else if (value.startsWith("item/")) {
                                    jsonObject.addProperty("parent", "item/" + nameConverter.getItemMapping().remap(value.substring("item/".length())));
                                }*/
                                //System.out.println("New parent: "+jsonObject.get("parent").getAsString());
                            }
                            if (jsonObject.has("overrides")) {
                                //System.out.println("Remapping parent");
                                JsonArray array = jsonObject.getAsJsonArray("overrides");
                                for (JsonElement jsonElement : array) {
                                    if (jsonElement instanceof JsonObject) {
                                        JsonObject value = (JsonObject) jsonElement;
                                        remap(value.get("model").getAsString(),"model",value,nameConverter);
                                        /*if (value.has("model")) {
                                            value.addProperty("model", remap(value.get("model").getAsString().substring("block/".length()),nameConverter));
                                        }*/
                                    }
                                }
                            }
                            Files.write(model, Collections.singleton(packConverter.getGson().toJson(jsonObject)), Charset.forName("UTF-8"));
                            jsonObject = Util.readJson(packConverter.getGson(), model);
                            content = jsonObject.toString();
                            if(content.contains("gold_helmet")) {
                                System.out.println("###################################"+model.toString());
                            }
                        } else if(model.toFile().isDirectory()) {
                            remapModelJson(model);
                        }
                    } catch (IOException e) {
                        throw Util.propagate(e);
                    }
                });
    }
    
    private void remap(String input, String key, JsonObject jsonObject, NameConverter nameConverter) {
        int pos = input.lastIndexOf("/")+1;
        String prefix="";
        String value=input;
        if(pos>0) {
            prefix = input.substring(0, pos);
            value = input.substring(pos);
        }
        Mapping mapping;
        if (prefix.startsWith("item/")) {
            mapping = nameConverter.getItemMapping();
        } else {
            mapping = nameConverter.getBlockMapping();
        }
        String converted = prefix + mapping.remap(value);
        jsonObject.addProperty(key, converted);
        /*
        
        if (input.startsWith("block/")) {
            jsonObject.addProperty(key, "block/" + nameConverter.getBlockMapping().remap(input.substring("block/".length())));
        } else if (input.startsWith("item/")) {
            jsonObject.addProperty(key, "item/" + nameConverter.getItemMapping().remap(input.substring("item/".length())));
        }

        String data = input;
        String converted = nameConverter.getBlockMapping().remap(data);
        Logger.getGlobal().info(data +" -> "+converted);
        if(data.contains("piston_head_normal")) {
        }
        return converted;*/
    }

}