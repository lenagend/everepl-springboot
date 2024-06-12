import json
from pytrends.request import TrendReq

def get_trending_urls():
    pytrends = TrendReq(hl='ko-KR', tz=540)  # 한국어로 설정하고 시간대를 서울로 설정 (UTC+9)
    trending_today = pytrends.trending_searches(pn='south_korea')  # 한국 지역 트렌드 가져오기
    urls = trending_today[0].tolist()
    return urls

if __name__ == "__main__":
    trending_urls = get_trending_urls()
    print(json.dumps(trending_urls, ensure_ascii=False, indent=4))  # UTF-8 인코딩으로 JSON 출력
