"""
    Script to generate a weaponRanges.json file of all weapons on the OSRS Wiki with an attack range greater than 1.
    The JSON file is placed in ../src/main/resources/weaponRanges.json.
    Originally written by jayktaylor and the OSRS DPS Calculator team at https://github.com/weirdgloop/osrs-dps-calc

    Written for Python 3.9.
"""
import os
import requests
import json
import urllib.parse

FILE_NAME = '../src/main/resources/weaponRanges.json'
WIKI_BASE = 'https://oldschool.runescape.wiki'
API_BASE = WIKI_BASE + '/api.php'

REQUIRED_PRINTOUTS = [
    'Item ID',
    'Weapon attack range',
    'Combat style'
]

def getEquipmentData():
    equipment = {}
    offset = 0
    while True:
        print('Fetching equipment info: ' + str(offset))
        query = {
            'action': 'ask',
            'format': 'json',
            'query': '[[Equipment slot::+]][[Item ID::+]]|?' + '|?'.join(REQUIRED_PRINTOUTS) + '|limit=500|offset=' + str(offset)
        }
        r = requests.get(API_BASE + '?' + urllib.parse.urlencode(query), headers={
            'User-Agent': 'in-range-plugin (https://github.com/thebeanbag/In-Range)'
        })
        data = r.json()

        if 'query' not in data or 'results' not in data['query']:
            # No results?
            break

        equipment = equipment | data['query']['results']

        if 'query-continue-offset' not in data or int(data['query-continue-offset']) < offset:
            # If we are at the end of the results, break out of this loop
            break
        else:
            offset = data['query-continue-offset']
    return equipment


def getPrintoutValue(prop):
    # SMW printouts are all arrays, so ensure that the array is not empty
    if not prop:
        return None
    else:
        return prop[0]

def main():
    # Grab the equipment info using SMW, including all the relevant printouts
    wiki_data = getEquipmentData()

    # Convert the data into our own JSON structure
    data = {}

    # Loop over the equipment data from the wiki
    for k, v in wiki_data.items():
        print('Processing ' + k)
        # Sanity check: make sure that this equipment has printouts from SMW
        if 'printouts' not in v:
            print(k + ' is missing SMW printouts - skipping.')
            continue

        po = v['printouts']
        item_id = getPrintoutValue(po['Item ID'])
        weapon_range = getPrintoutValue(po['Weapon attack range']) or 0
        category = getPrintoutValue(po['Combat style']) or ''

        if category == 'Staff' or category == 'Bladed Staff':
            weapon_range = 10

        if weapon_range < 2:
            continue
        # Append the current equipment item to the calc's equipment list
        data[item_id] = weapon_range

    print('Total equipment: ' + str(len(data)))

    with open(FILE_NAME, 'w') as f:
        print('Saving to JSON at file: ' + FILE_NAME)
        json.dump(data, f, ensure_ascii=False, indent=2)

main()
