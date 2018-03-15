require 'cassandra'
require 'date'
require 'radish/documents/core'

require_relative '../lib/local_env'
require_relative '../lib/timezones'

module Services
  class Tables
    include Radish::Documents::Core

    LOCAL_ENV = LocalEnv.new(
      'CASSANDRA', {
        hosts:    { type: :list,   default: ['localhost'] },
        keyspace: { type: :string, default: 'xadf' },
      })
    

    def initialize()
      begin
        hosts = LOCAL_ENV.get(:hosts)
        keyspace = LOCAL_ENV.get(:keyspace)

        puts "> discovering cluster (hosts=#{hosts})"
        cluster = ::Cassandra.cluster(hosts: hosts)

        puts "> connecting to keyspace (keyspace=#{keyspace})"
        @session = cluster.connect(keyspace)

        puts '< connected'
      rescue ::Cassandra::Errors::NoHostsAvailable => e
        puts '! no available Cassandra instance'
        p e
      rescue ::Cassandra::Errors::IOError => e
        puts '! failed to connect to cassandra'
        p e
      rescue ::Cassandra::Errors::InvalidError => e
        puts '! failed to connect to cassandra'
        p e
      end        
      @tzs = Timezones.new
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
        codes[:country] = get(address, 'country.name') if !codes[:country] && has(address, 'country.name')
        codes[:region] = get(address, 'subentity.code.value') if has(address, 'subentity.code.value')
        codes[:region] = get(address, 'subentity.name') if !codes[:region] && has(address, 'subentity.name')
      end
    end

    def find_jurisdiction(o)
      @juri_paths ||= ['address', 'location.address']
      address = @juri_paths.map { |path| get(o, path) }.drop_while(&:nil?).first
      if address
        codes = find_codes(address)
        tz = @tzs.lookup(
          { name: get(address, 'country.name'), code: get(address, 'country.code.value') },
          { name: get(address, 'subentity.name'), code: get(address, 'subentity.code.value') },
          get(address, 'city'))
            
        codes.merge(timezone: tz)
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
