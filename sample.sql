    -- Enable UUID extension
    create extension if not exists "uuid-ossp";

    -- 1. profiles
    create table if not exists profiles (
        id uuid references auth.users on delete cascade primary key,
        username text unique,
        full_name text,
        avatar_url text,
        created_at timestamptz default now(),
        updated_at timestamptz default now()
    );

    -- 2. user_settings
    create table if not exists user_settings (
        id uuid primary key default uuid_generate_v4(),
        user_id uuid references profiles(id) on delete cascade not null,
        theme text default 'SYSTEM',
        notifications_enabled boolean default true,
        reminder_time text,
        weekly_report_enabled boolean default true,
        language text default 'en',
        updated_at timestamptz default now(),
        unique(user_id)
    );

    -- 3. goals
    create table if not exists goals (
        id uuid primary key default uuid_generate_v4(),
        user_id uuid references profiles(id) on delete cascade not null,
        title text not null,
        description text,
        category text default 'OTHER',
        priority text default 'MEDIUM',
        status text default 'ACTIVE',
        target_value numeric default 100.0,
        current_value numeric default 0.0,
        unit text default '%',
        start_date timestamptz default now(),
        target_date timestamptz not null,
        completed_at timestamptz,
        created_at timestamptz default now(),
        updated_at timestamptz default now()
    );

    -- 4. progress_updates
    create table if not exists progress_updates (
        id uuid primary key default uuid_generate_v4(),
        goal_id uuid references goals(id) on delete cascade not null,
        user_id uuid references profiles(id) on delete cascade not null,
        value numeric not null,
        note text,
        logged_at timestamptz default now(),
        created_at timestamptz default now()
    );

    -- 5. goal_milestones
    create table if not exists goal_milestones (
        id uuid primary key default uuid_generate_v4(),
        goal_id uuid references goals(id) on delete cascade not null,
        title text not null,
        target_value numeric default 0.0,
        is_completed boolean default false,
        completed_at timestamptz,
        created_at timestamptz default now()
    );

    -- 6. goal_statistics view
    create or replace view goal_statistics as
    select 
        user_id,
        count(id)::int as total_goals,
        count(case when status = 'ACTIVE' then 1 end)::int as active_goals,
        count(case when status = 'COMPLETED' then 1 end)::int as completed_goals,
        case when count(id) > 0 then (count(case when status = 'COMPLETED' then 1 end)::numeric / count(id)) * 100 else 0 end as completion_rate,
        avg(case when target_value > 0 then (current_value / target_value) * 100 else 0 end) as avg_progress_percent,
        count(case when target_date >= now() and target_date < now() + interval '7 days' and status != 'COMPLETED' then 1 end)::int as goals_due_this_week
    from goals
    group by user_id;

    -- Enable Row Level Security (RLS)
    alter table profiles enable row level security;
    alter table user_settings enable row level security;
    alter table goals enable row level security;
    alter table progress_updates enable row level security;
    alter table goal_milestones enable row level security;

    -- Create policies
    create policy "Users can view their own profile." on profiles for select using (auth.uid() = id);
    create policy "Users can update their own profile." on profiles for update using (auth.uid() = id);

    create policy "Users can view their own settings." on user_settings for select using (auth.uid() = user_id);
    create policy "Users can update their own settings." on user_settings for update using (auth.uid() = user_id);
    create policy "Users can insert their own settings." on user_settings for insert with check (auth.uid() = user_id);

    create policy "Users can view their own goals." on goals for select using (auth.uid() = user_id);
    create policy "Users can create their own goals." on goals for insert with check (auth.uid() = user_id);
    create policy "Users can update their own goals." on goals for update using (auth.uid() = user_id);
    create policy "Users can delete their own goals." on goals for delete using (auth.uid() = user_id);

    create policy "Users can view their own progress updates." on progress_updates for select using (auth.uid() = user_id);
    create policy "Users can create their own progress updates." on progress_updates for insert with check (auth.uid() = user_id);
    create policy "Users can update their own progress updates." on progress_updates for update using (auth.uid() = user_id);
    create policy "Users can delete their own progress updates." on progress_updates for delete using (auth.uid() = user_id);

    create policy "Users can view milestones for their goals." on goal_milestones for select using (
        goal_id in (select id from goals where user_id = auth.uid())
    );
    create policy "Users can create milestones for their goals." on goal_milestones for insert with check (
        goal_id in (select id from goals where user_id = auth.uid())
    );
    create policy "Users can update milestones for their goals." on goal_milestones for update using (
        goal_id in (select id from goals where user_id = auth.uid())
    );
    create policy "Users can delete milestones for their goals." on goal_milestones for delete using (
        goal_id in (select id from goals where user_id = auth.uid())
    );
   