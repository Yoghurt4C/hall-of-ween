import json
from pathlib import Path

DISCOVERY_FOOD = ["candy_corn_cake", "candy_corn_cake_slice", "candy_corn_custard", "spicy_pumpkin_cookie",
                  "candied_apple", "candy_corn_cookie", "glazed_pumpkin_pie", "homemade_campfire_treat"]
TOT_MATS = ["chattering_skull", "nougat_center", "plastic_fangs"]


def gen_everything():
    data = "src/main/resources/data/hallofween/"
    recipes = data + "recipes/"
    adv = data + "advancements/discovery/"
    gen_initial_discovery_food_data(recipes, adv)
    gen_temporary_tot_mat_recipes(recipes)


def gen_temporary_tot_mat_recipes(recipes):
    for item in TOT_MATS:
        output = recipes + "temp/" + item + ".json"
        if not (Path(output).exists()):
            obj = {
                "type": "minecraft:crafting_shapeless",
                "ingredients": [
                    {"item": "hallofween:" + item}
                ],
                "result": {
                    "item": "",
                    "count": 2
                }
            }

            if item == TOT_MATS[0]:
                obj["result"]["item"] = "minecraft:bone"
            elif item == TOT_MATS[1]:
                obj["result"]["item"] = "minecraft:gunpowder"
            elif item == TOT_MATS[2]:
                obj["result"]["item"] = "minecraft:string"

            with open(output, "w") as file:
                json.dump(obj, file, indent=4, sort_keys=False)
                file.close()

        output = recipes + "temp/" + item + "_conversion.json"
        if not (Path(output).exists()):
            obj = {
                "type": "minecraft:crafting_shapeless",
                "ingredients": [
                    {"item": "hallofween:candy_corn_glaze"}
                ],
                "result": {
                    "item": "hallofween:" + item,
                    "count": 3
                }
            }

            temp = TOT_MATS.copy()
            temp.remove(item)
            for thing in temp:
                for i in range(2):
                    obj["ingredients"].append({"item": "hallofween:" + thing})

            with open(output, "w") as file:
                json.dump(obj, file, indent=4, sort_keys=False)
            file.close()


def gen_initial_discovery_food_data(recipes, adv):
    adv += "candy_corn/"
    for item in DISCOVERY_FOOD:
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

