from glob import glob
from pathlib import Path
import os
import json


def gen_item_models_from_sprites():
	assets = "src/main/resources/assets/hallofween/"
	sprites = glob("" + assets + "textures/item/*.png")
	output_path = assets + "models/item/"
	Path(output_path).mkdir(parents=True, exist_ok=True)
	for filename in sprites:
		name = os.path.basename(filename).split('.')[0]
		if Path(output_path + name + ".json").exists():
			continue
		else:
			obj = {
				"parent": "minecraft:item/generated",
				"textures": {
					"layer0": "hallofween:item/" + name
				}
			}
			with open(output_path + name + ".json", "w") as model:
				json.dump(obj, model, indent=4, sort_keys=False)
				model.close()

	sprites = glob("" + assets + "textures/item/container/*.png")
	textures = []
	overlayTextures = []
	for filename in sprites:
		name = os.path.basename(filename).split('.')[0]
		nooverlay = name.removesuffix("_overlay")
		if not name == nooverlay:
			overlayTextures.append(nooverlay)
		textures.append(nooverlay)

	for texture in textures:
		obj = {
			"parent": "minecraft:item/generated",
			"textures": {
				"layer0": "hallofween:item/container/" + texture
			}
		}
		if overlayTextures.__contains__(texture):
			obj["textures"]["layer1"] = "hallofween:item/container/" + texture + "_overlay"

		with open(output_path + "container/" + texture + ".json", "w") as model:
			json.dump(obj, model, indent=4, sort_keys=False)
			model.close()


gen_item_models_from_sprites()
