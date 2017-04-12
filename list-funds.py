# coding=utf-8
# generates funds.csv, website/funds.json from baud.bg

from lxml import html
import requests
page = requests.get('http://baud.bg/quotes/')
tree = html.fromstring(page.content)

def translate(bg):
    unchanged = u'- '
    cyr = u"абвгдезийклмнопрстуфхъц"
    cyr += cyr.upper()
    lat = u"abvgdeziyklmnoprstufhyc"
    lat += lat.upper()
    complex_map = {u'ю': 'yu', u'я': 'ya', u'ж': 'zh', u'ш': u'sh', u'щ': 'sht', u'ц': 'ts', u'ч': 'ch'}
    for k in list(complex_map.keys()):
        complex_map[k.upper()] = complex_map[k].upper()
    res = ''
#     print cyr
    for l in bg:
        if l in unchanged or re.search('[-a-z0-9]', l ,re.IGNORECASE):
            res += l
        else:
#             print l, cyr.find(l)
            try:
                res += lat[cyr.index(l)]
            except:
                if l in complex_map:
                    res += complex_map[l]
#             print res
    return res

import re

to_json = []
comma_sep = u''
for t in tree.xpath('..//div[@class="fond_div"]/a'):
    fund_id = (re.search('fond=([0-9]+)', t.attrib['href']).group(1)).strip()
    bg_name = t.text_content().strip()
    if not bg_name: continue
    lat_name = translate(bg_name).strip().replace(' ', '-')
    lat_name = re.sub('--+', '-', lat_name)

    print(fund_id, bg_name, lat_name)
    comma_sep += fund_id + ',' + bg_name + ',' + lat_name + '\n'
    to_json.append((fund_id, bg_name, lat_name))
with open('funds.csv', 'wb') as f:
    f.write(comma_sep.encode("UTF-8"))

import json
with open('website/funds.json', 'w') as f:
    json.dump(to_json, f)
print ('wrote funds.csv, website/funds.json')
