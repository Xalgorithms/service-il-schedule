require 'yaml'

subdivisions = Dir.glob('iso_data/subdivisions/*.yaml').inject({}) do |all, fn|
  country_code = File.basename(fn, '.yaml')
  o = YAML.load(IO.read(fn))
  div = o.map do |(k, v)|
    { code: k, name: v['name'] }.tap do |o|
      o[:geo] = { lat: v['geo']['latitude'], lon: v['geo']['longitude'] } if v.key?('geo') && v['geo']
    end
  end
  all.merge(country_code => div)
end

countries = Dir.glob('iso_data/countries/*.yaml').inject({}) do |all, fn|
  code = File.basename(fn, '.yaml')
  o = YAML.load(IO.read(fn))[code]

  country = {
    name: o['name'],
    code2: o['alpha2'],
    code3: o['alpha3'],
    continent: o['continent'],
    region: o['region'],
    subregion: o['subregion'],
  }.tap do |c|
    c[:geo] = { lat: o['geo']['latitude'], lon: o['geo']['longitude'] } if o.key?('geo') && o['geo']
  end

  all.merge(code => country)
end

subdivisions.each do |(country_code, subdivisions)|
  File.open("app/services/subdivisions/#{country_code}.scala", "w+") do |f|
    elems = subdivisions.map do |o|
      geo = o.key?(:geo) ? "LatLon(\"#{o[:geo][:lat]}\", \"#{o[:geo][:lon]}\")" : "null"
      "\t\tSubdivision(\"#{o[:name]}\", \"#{o[:code]}\", #{geo})"
    end
    f.write("package services.subdivisions\n\n")
    f.write("import services.{ LatLon, Subdivision }\n\n")
    f.write("object #{country_code} {\n")
    f.write("\tval ALL = Seq(\n")
    f.write(elems.join(",\n"))
    f.write("\n\t)\n")
    f.write("}\n")
  end
end

File.open("app/services/Countries.scala", "w+") do |f|
  f.write("package services\n\n")
  f.write("object Countries {\n")
  f.write("\tval ALL = Map(\n")
  elems = countries.map do |(code, country)|
    subs = subdivisions.key?(code) ? "services.subdivisions.#{code}.ALL" : "Seq()"
    geo = country.key?(:geo) ? "LatLon(\"#{country[:geo][:lat]}\", \"#{country[:geo][:lon]}\")" : "null"
    args = [
      "\"#{country[:name]}\"",
      "\"#{country[:code2]}\"",
      "\"#{country[:code3]}\"",
      "\"#{country[:continent]}\"",
      "\"#{country[:region]}\"",
      "\"#{country[:subregion]}\"",
      subs,
      "#{geo}"
    ]


    "\t\t\"#{code}\" -> Country(#{args.join(', ')})"
  end
  f.write(elems.join(",\n"))
  f.write("\n\t)\n")
  f.write("}\n")
end

