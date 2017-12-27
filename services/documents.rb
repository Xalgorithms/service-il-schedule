require 'uuid'

module Services
  class Documents
    def initialize(opts)
    end
    
    def store(doc)
      id = UUID.generate
      id
    end
  end
end
