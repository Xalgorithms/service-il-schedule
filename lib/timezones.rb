require 'countries'
require 'geocoder'
require 'timezone'

# generate API key: https://developers.google.com/maps/documentation/timezone/get-api-key
class Timezones
  def initialize
    Timezone::Lookup.config(:google) do |c|
      c.api_key = ENV.fetch('GOOGLE_TIMEZONE_API_KEY', nil)
    end
    Geocoder.configure(lookup: :nominatim)
  end

  def lookup(country, region, city)
    country = normalize_country(country)
    region = normalize_region(country, region) if region
    lookup_timezone_by_location(lookup_location(country, region, city))
  end

  private

  def normalize_country(country)
    c = ISO3166::Country.new(country[:code])
    c = ISO3166::Country.new(country[:name]) if !c
    c = ISO3166::Country.find_country_by_name(country[:name]) if !c
    c = ISO3166::Country.find_country_by_alpha3(country[:code]) if !c
    c = ISO3166::Country.find_country_by_alpha3(country[:name]) if !c
    c
  end

  def normalize_region(country, region)
    # incoming ISO8166-2 regions are <country_code>-<region_code>
    region_code = (region[:code] || region[:name] || '').split('-').last
    r = country.states[region_code]
    # strangely, country.states is a Hash with the code as a key, but it is
    # often not set in the code property of the value
    r = country.states.map do |code, st|
      st.code = code
      st
    end.find do |st|
      st.name == region[:name]
    end if !r

    r
  end

  def lookup_location(country, region, city)
    loc = lookup_location_by_region(region) || { lat: country.geo['latitude'], lon: country.geo['longitude'] }
    if city
      # if we actually have a city, we can search online with precision
      loc = lookup_location_by_city(country, city)
    end

    loc
  end

  def lookup_location_by_city(country, city)
    m = Geocoder.search(city, params: { countrycodes: country.alpha2 }).select do |res|
      address = res.data['address']
      t = res.data['type']

      t == 'city' && address['country_code'] == country.alpha2.downcase
    end.map(&:data).first

    m ? { lat: m['lat'], lon: m['lon'] } : nil
  end

  def lookup_location_by_region(region)
    if region && region.geo && region.geo['latitude']
      {
        lat: region.geo['latitude'],
        lon: region.geo['longitude'],
      }
    end
  end
  
  def lookup_timezone_by_location(loc)
    if loc
      tz = Timezone.lookup(loc[:lat], loc[:lon])
      tz ? tz.name : nil
    end
  end
end
