# coding=utf-8
# generates funds.csv, website/funds.json from baud.bg

from __future__ import print_function
from lxml import html
import requests
from collections import defaultdict

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

project_dir = './'
import datetime
import time

def to_date_obj(dashed_date):
    return datetime.datetime.strptime(dashed_date,'%Y-%m-%d').date()

def get_fund_filename(lat_name):
    return project_dir + 'website/data/' + lat_name + '-daily.csv'

def get_last_csv_row(fund_name):
    with open(get_fund_filename(fund_name), 'r') as f:
        while True:
            ln = f.readline()
            if ln:
                line = ln
            else: break
    return line.strip().split(',')

def is_business_day(dt):
    return dt.isoweekday()<=5
# is_business_day(dt)

def add_business_days(date, days):
    assert days >= 0, 'only positive days supported'
    day = datetime.timedelta(days=1)
    while days > 0:
        if is_business_day(date):
            days -= 1
        date = date + day
    return date

def download_range(fund_id, start_date):
    today = datetime.date.today()
    
    url = 'http://baud.bg/quotes/?search_date=' + today.strftime('%d.%m.%Y') + '&start_date=%s'%start_date+\
        '&end_date=%s'%today+'&currency=&companyes=&type=&fond='+fund_id+'&jr='
    print('downloading', url)
    #print 'DEBUG skipped download'
    page = requests.get(url)
    tree = html.fromstring(page.content)
    fund = parse_fund_div(tree.xpath('..//div[@class="fond_div"]')[0])
    assert fund.fund_id == fund_id, 'requested fund_id != delivered fund_id %s, %s'%(fund_id, fund.fund_id)
    daily = get_fund_filename(fund.lat_name)

    #some quotes are wrong - best visible in charts, make a registry of funds and dates to skip
    drop_quotes = defaultdict(set, {11:{'2009-04-21'},40:{'2014-01-22'},41:{'2012-02-07'},54:'2009-08',85:{'2012-12-18'},89:{'2013-04-18'}}) #zeros are significant

    drop_quotes[85].add("2012-12-18")
    drop_quotes[40].add("2014-01-22")
    drop_quotes[37].add("2016-04-08")

    with open(daily,'a') as f:
        if not f.tell():
            f.write('Дата,НСА (общо),НСА (дял),Дялове\n')
        print('append ', len(fund.quotes), 'quotes to', daily)
        for date, unit_price, mcap in fund.quotes:
            #print 'date, unit_price, mcap', date, unit_price, mcap
            #zeros in rows http://baud.bg/quotes/?search_date=03.05.2017&start_date=2006-04-17&end_date=2017-05-03&currency=&companyes=&type=&fond=11&jr=
            if mcap != 0 and unit_price !=0 and unit_price < 5000 and unit_price < mcap and str(date) not in drop_quotes[int(fund_id)]:
                line = '%s,%.2f,%f,%.2f\n'%(date, mcap, unit_price,mcap/unit_price)
                #print line,
                f.write('%s,%f,%f,%f\n'%(date, mcap, unit_price,mcap/unit_price))

class Fund():
    pass
def parse_fund_div(fund_div):
    fund = Fund()
    link = fund_div.xpath('a')[0]
    fund.fund_id = (re.search('fond=([0-9]+)', link.attrib['href']).group(1)).strip()
    fund.bg_name = link.text_content().strip()
    #if not bg_name: continue
    lat_name = translate(fund.bg_name).strip().replace(' ', '-')
    fund.lat_name = re.sub('--+', '-', lat_name)
    
    #get IPO date
    #Дата на публично предлагане:
    ipo_date = fund_div.xpath(u'.//text()[contains(.,"Дата на публично предлагане")]/following-sibling::strong/text()')[0]
    fund.ipo_date = to_date_obj(to_csv_date(ipo_date))
    fund.quotes = []

    for tr in fund_div.xpath('table/tr[position()>1]'):
        cells = list(map(html.HtmlElement.text_content, tr.xpath('td')))
        fund.quote_date = to_date_obj(to_csv_date(cells[-1]))
        fund.mcap, fund.unit_price = [float(f.split()[0]) for f in cells[-3:-1]]
        fund.quotes.append((fund.quote_date, fund.unit_price, fund.mcap))
    return fund

def to_csv_date(dotted_date):
    return '-'.join(reversed(dotted_date.split('.')))

to_json = []
comma_sep = u''
i = 0
for fund_div in tree.xpath('..//div[@class="fond_div"]'):

    fund = parse_fund_div(fund_div)
    quote_date = fund.quotes[0][0]
    print('downloaded date', quote_date)

    try:
        last_csv_date = to_date_obj(get_last_csv_row(fund.lat_name)[0])
        print('last_csv_date', last_csv_date)
        start_date = add_business_days(last_csv_date, 1)
    except IOError:
        start_date = fund.ipo_date
    #print last_csv_date, fund.quote_date
    if start_date >= quote_date:
        print('no need for ranged request')
    else:
        print('download from', start_date)
        download_range(fund.fund_id, start_date)
    
    print(fund.fund_id, fund.bg_name, fund.lat_name, fund.ipo_date)
    comma_sep += fund.fund_id + ',' + fund.bg_name + ',' + fund.lat_name + ',%s'%fund.ipo_date + '\n'
    to_json.append((fund.fund_id, fund.bg_name, fund.lat_name, str(fund.ipo_date)))
    i += 1
    #if i == 30: break
with open(project_dir+'funds.csv', 'wb') as f:
    f.write(comma_sep.encode("UTF-8"))

import json
with open('website/funds.json', 'w') as f:
    json.dump(to_json, f)
print('wrote funds.csv, website/funds.json')
