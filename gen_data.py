import json
from pathlib import Path

ITEMS = ["candy_corn_cake", "candy_corn_cake_slice", "candy_corn_custard", "spicy_pumpkin_cookie", "candied_apple",
         "candy_corn_cookie", "glazed_pumpkin_pie", "homemade_campfire_treat"]


def gen_everything():
    data = "src/main/resources/data/hallofween/"
    adv = data + "advancements/discovery/candy_corn/"
    recipes = data + "recipes/"
    for item in ITEMS:
        output = adv + item + ".json"
        if not (Path(output).exists()):
            obj = {
                "parent": "hallofween:discovery/root",
                "rewards": {
                    "recipes": [
                        "hallofween:" + item
                    ]
                },
                "criteria": {
                    "has_the_recipe": {
                        "trigger": "minecraft:consume_item",
                        "conditions": {
                            "item": {
                                "items": [
                                    "hallofween:recipe_sheet"
                                ],
                                "nbt": "{targetItem: \"hallofween:" + item + "\"}"
                            }
                        }
                    }
                },
                "requirements": [
                    [
                        "has_the_recipe"
                    ]
                ]
            }

            with open(output, "w") as file:
                json.dump(obj, file, indent=4, sort_keys=False)
            file.close()

        output = recipes + item + ".json"
        if not (Path(output).exists()):
            obj = {
                "type": "minecraft:crafting_shapeless",
                "ingredients": [
                    {"item":  ""},
                    {"item":  ""}
                ],
                "result": {
                    "item": "hallofween:" + item
                },
                "advancement": "hallofween:discovery/candy_corn/" + item
            }

            with open(output, "w") as file:
                json.dump(obj, file, indent=4, sort_keys=False)
            file.close()


gen_everything()