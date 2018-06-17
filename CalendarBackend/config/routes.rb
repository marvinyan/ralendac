Rails.application.routes.draw do
  constraints subdomain: "api" do
    scope module: "api" do
      namespace :v1, defaults: { format: :json } do
        resources :events, only: %i[index create update destroy]
      end
    end
  end
end
