require_relative '../lib/timezones'

describe Timezones do
  def validate()
  end
  
  it 'should provide valid timezone identifiers for city, region, country' do
    expects = [
      {
        city:    'Toronto',
        region:  { name: 'Ontario', code: 'CA-ON' },
        country: { name: 'Canada', code: 'CA', code3: 'CAN' },
        geocode: {
          lat: '43.653963',
          lon: '-79.387207',
        },
        ex: 'America/Toronto',
      },
      {
        city:    'Halifax',
        region:  { name: 'Nova Scotia', code: 'CA-NS' },
        country: { name: 'Canada', code: 'CA', code3: 'CAN' },
        geocode: {
          lat: '44.6486237',
          lon: '-63.5859487',
        },
        ex: 'America/Halifax',
      },
      {
        city:    'Newark',
        region:  { name: 'New Jersey', code: 'NJ' },
        country: { name: 'United States of America', code: 'US', code3: 'USA' },
        geocode: {
          lat: '40.735657',
          lon: '-74.1723667',
        },
        ex: 'America/New_York',
      },
      {
        city:    'Liverpool',
        region:  { name: 'Liverpool', code: 'GB-LIV' },
        country: { name: 'United Kingdom of Great Britain and Northern Ireland', code: 'GB', code3: 'GBR' },
        geocode: {
          lat: '53.4054719',
          lon: '-2.9805392',
        },
        ex: 'Europe/London',
      },
    ]

    expects.each do |t|
      expect(Timezone::Lookup).to receive(:config).with(:google)
      tzs = Timezones.new

      geocoder_args = [t[:city], params: { countrycodes: t[:country][:code] }]
      geocoder_res = OpenStruct.new(data: {
        'type' => 'city',
        'lat' => t[:geocode][:lat],
        'lon' => t[:geocode][:lon],
        'address' => {
          'state' => t[:region][:name],
          'country' => t[:country][:name],
          'country_code' => t[:country][:code].downcase,
        },
      })
      tz_res = OpenStruct.new(name: t[:ex])
      
      expect(Geocoder).to receive(:search).exactly(7).times.with(*geocoder_args).and_return([geocoder_res])
      expect(Timezone).to receive(:lookup).exactly(7).times.with(t[:geocode][:lat], t[:geocode][:lon]).and_return(tz_res)
                        
      expect(tzs.lookup(t[:country], t[:region], t[:city])).to eql(t[:ex])
      # try again with the only the codes
      expect(tzs.lookup({ code: t[:country][:code] }, { code: t[:region][:code] }, t[:city])).to eql(t[:ex])
      # try again with the codes in the name
      expect(tzs.lookup(
               { name: t[:country][:code], code: nil },
               { name: t[:region][:code], code: nil },
               t[:city])).to eql(t[:ex])
      # just the country name, no code
      expect(tzs.lookup(
               { name: t[:country][:name] },
               { name: t[:region][:code], code: nil },
               t[:city])).to eql(t[:ex])
      # just the region name, no code
      expect(tzs.lookup(
               t[:country],
               { name: t[:region][:name] },
               t[:city])).to eql(t[:ex])
      # country can be alpha3 in name or code
      expect(tzs.lookup({ code: t[:country][:code3] }, t[:region], t[:city])).to eql(t[:ex])
      expect(tzs.lookup({ name: t[:country][:code3] }, t[:region], t[:city])).to eql(t[:ex])
    end
  end

  it 'should provide timezones based on the region if there is no city specified' do
    expects = [
      {
        region:  { name: 'Ontario', code: 'CA-ON' },
        country: { name: 'Canada', code: 'CA', code3: 'CAN' },
        region_code: 'ON',
        ex: 'America/Toronto',
      },
      {
        region:  { name: 'Nova Scotia', code: 'CA-NS' },
        country: { name: 'Canada', code: 'CA', code3: 'CAN' },
        region_code: 'NS',
        ex: 'America/Halifax',
      },
      {
        region:  { name: 'New Jersey', code: 'NJ' },
        country: { name: 'United States of America', code: 'US', code3: 'USA' },
        region_code: 'NJ',
        ex: 'America/New_York',
      },
      {
        region:  { name: 'Liverpool', code: 'GB-LIV' },
        country: { name: 'United Kingdom of Great Britain and Northern Ireland', code: 'GB', code3: 'GBR' },
        region_code: 'LIV',
        ex: 'Europe/London',
      },
    ]

    expects.each do |t|
      expect(Timezone::Lookup).to receive(:config).with(:google)
      tzs = Timezones.new

      country = ISO3166::Country.new(t[:country][:code])
      region = country.subdivisions[t[:region_code]]
      tz_res = OpenStruct.new(name: t[:ex])
      
      expect(Timezone).to receive(:lookup).exactly(7).times.with(region.geo['latitude'], region.geo['longitude']).and_return(tz_res)
                        
      expect(tzs.lookup(t[:country], t[:region], nil)).to eql(t[:ex])
      # try again with the only the codes
      expect(tzs.lookup({ code: t[:country][:code] }, { code: t[:region][:code] }, nil)).to eql(t[:ex])
      # try again with the codes in the name
      expect(tzs.lookup(
               { name: t[:country][:code], code: nil },
               { name: t[:region][:code], code: nil },
               nil)).to eql(t[:ex])
      # just the country name, no code
      expect(tzs.lookup(
               { name: t[:country][:name] },
               { name: t[:region][:code], code: nil },
               nil)).to eql(t[:ex])
      # just the region name, no code
      expect(tzs.lookup(
               t[:country],
               { name: t[:region][:name] },
               nil)).to eql(t[:ex])
      # country can be alpha3 in name or code
      expect(tzs.lookup({ code: t[:country][:code3] }, t[:region], nil)).to eql(t[:ex])
      expect(tzs.lookup({ name: t[:country][:code3] }, t[:region], nil)).to eql(t[:ex])
    end
  end

  it 'should provide timezones based on the country if there is neither a city nor a region' do
    expects = [
      {
        country: { name: 'Canada', code: 'CA', code3: 'CAN' },
        ex: 'America/Regina',
      },
      {
        country: { name: 'United States of America', code: 'US', code3: 'USA' },
        ex: 'America/Chicago',
      },
      {
        country: { name: 'United Kingdom of Great Britain and Northern Ireland', code: 'GB', code3: 'GBR' },
        ex: 'Europe/London',
      },
    ]

    expects.each do |t|
      expect(Timezone::Lookup).to receive(:config).with(:google)
      tzs = Timezones.new

      country = ISO3166::Country.new(t[:country][:code])
      tz_res = OpenStruct.new(name: t[:ex])

      expect(Timezone).to receive(:lookup).exactly(7).times.with(country.geo['latitude'], country.geo['longitude']).and_return(tz_res)
                        
      expect(tzs.lookup(t[:country], nil, nil)).to eql(t[:ex])
      # region is full of nils
      expect(tzs.lookup(t[:country], { name: nil, code: nil }, nil)).to eql(t[:ex])
      # try again with the only the codes
      expect(tzs.lookup({ code: t[:country][:code] }, nil, nil)).to eql(t[:ex])
      # try again with the codes in the name
      expect(tzs.lookup({ name: t[:country][:code], code: nil }, nil, nil)).to eql(t[:ex])
      # try again with just the name
      expect(tzs.lookup({ name: t[:country][:name] }, nil, nil)).to eql(t[:ex])
      # country can be alpha3 in name or code
      expect(tzs.lookup({ code: t[:country][:code3] }, nil, nil)).to eql(t[:ex])
      expect(tzs.lookup({ name: t[:country][:code3] }, nil, nil)).to eql(t[:ex])
    end
  end
end
