from glob import glob
from pathlib import Path
import os
import json


def gen_item_models_from_sprites():
	assets = "src/main/resources/assets/hallofween/"
	sprites = glob("" + assets + "textures/item/*.png")
	for filename in sprites:
		name = os.path.basename(filename).split('.')[0]
		output_path = assets + "models/item/"
		Path(output_path).mkdir(parents=True, exist_ok=True)
		if name.startswith("trick_or_treat_bag") | Path(output_path + name + ".json").exists():
			continue
		obj = {
			"parent": "minecraft:item/generated",
			"textures": {
				"layer0": "hallofween:item/" + name
			}
		}
		with open(output_path + name + ".json", "w") as model:
			json.dump(obj, model, indent=4, sort_keys=False)
			model.close()


gen_item_models_from_sprites()
