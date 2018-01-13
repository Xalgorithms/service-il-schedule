require 'active_support/values/time_zone'
require 'cassandra'
require 'date'
require 'radish/documents/core'

module Services
  class Tables
    include Radish::Documents::Core
    
    def initialize(opts)
      cluster = ::Cassandra.cluster(
        hosts: opts['hosts'],
        port: opts['port'])
      @session = cluster.connect(opts['keyspace'])
    end

    def store_envelope(id, payload)
      @expected_parties ||= ['supplier', 'customer', 'payee', 'buyer', 'seller', 'tax']
      issued = DateTime.parse(payload['issued']).to_s
      juris = @expected_parties.inject({}) do |o, n|
        juri = find_jurisdiction(get(payload, "parties.#{n}"))
        juri ? o.merge(n => juri) : o
      end

      inserts = juris.map do |n, juri|
        juri.merge(document_id: id, issued: issued, party: n)
        # country: juri[:country],
        # region: juri[:region],
        # timezone: 
      end

      within_batch do
        build_inserts(
          'xadf.envelopes',
          [:document_id, :issued, :party, :country, :region, :timezone],
          inserts)
      end
    end

    private

    def find_codes(address)
      {}.tap do |codes|
        codes[:country] = get(address, 'country.code.value') if has(address, 'country.code.value')
        codes[:region] = get(address, 'subentity.code.value') if has(address, 'subentity.code.value')
      end
    end

    def find_jurisdiction(o)
      @juri_paths ||= ['address', 'location.address']
      address = @juri_paths.map { |path| get(o, path) }.drop_while(&:nil?).first
      if address
        codes = find_codes(address)
        country = codes.fetch(:country, nil)
        if country
          codes.merge(timezone: ActiveSupport::TimeZone.country_zones(country).first.tzinfo.identifier)
        end
      end
    end

    def build_inserts(tn, ks, os)
      os.map do |o|
        avail_ks = ks.select { |k| o.key?(k) && o[k] }
        vals = avail_ks.map { |k| "'#{o[k]}'" }
        "INSERT INTO #{tn} (#{avail_ks.join(',')}) VALUES (#{vals.join(',')})"
      end.join('; ') + ';'
    end

    def within_batch
      q = 'BEGIN BATCH ' + yield + ' APPLY BATCH;'
      stm = @session.prepare(q)
      @session.execute(stm)
    end    
  end
end
